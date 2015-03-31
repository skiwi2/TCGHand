package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class TCGHandGame extends ApplicationAdapter {
	private static final float CARD_WIDTH = 1f;
	private static final float CARD_HEIGHT = 1.5f;
	private static final float CARD_DEPTH = 0.01f;

	private TweenManager tweenManager;

	private PerspectiveCamera camera;
	private CameraInputController cameraInputController;

	private Texture cardTexture;

	private ModelBatch modelBatch;
	private Model redModel;
	private Model blueModel;
	private Array<ModelInstance> handInstances = new Array<ModelInstance>();
	private Array<ModelInstance> deckInstances = new Array<ModelInstance>();

	private Deck deck;
	private Hand hand;

	private Environment environment;

	@Override
	public void create() {
		super.create();

		Tween.registerAccessor(ModelInstance.class, new ModelInstanceAccessor());
		tweenManager = new TweenManager();

		camera = new PerspectiveCamera(60f, 800, 600);
		camera.position.set(0f, 1f, 3f);
		camera.lookAt(0f, 0f, 0f);
		camera.near = 0.1f;
		camera.far = 1000f;
		camera.update();
		cameraInputController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraInputController);

		cardTexture = new Texture(Gdx.files.internal("Fighter.png"));

		modelBatch = new ModelBatch();
		redModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
			new Material(ColorAttribute.createDiffuse(Color.RED), TextureAttribute.createDiffuse(cardTexture)),
			Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		blueModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
			new Material(ColorAttribute.createDiffuse(Color.BLUE), TextureAttribute.createDiffuse(cardTexture)),
			Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		addCardToHand();
		recalculateCardPositions();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		for (int i = 0; i < 60; i++) {
			addCardToDeck();
		}
		recalculateDeckPositions();

		deck = new Deck();
		for (int i = 0; i < 60; i++) {
			deck.addCard(cardTexture);
		}
		deck.transform.translate(-3f, 0f, 2f);

		hand = new Hand();
		hand.transform.translate(0f, 0f, -1f);
	}

	@Override
	public void render() {
		super.render();

		Gdx.gl.glClearColor(0.2f, 0f, 0f, 1f);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.getPitch() >= 5f || Gdx.input.getRoll() >= 5f) {
			hand.addCard(deck.drawCard());
			addCardToHand();
			recalculateCardPositions();
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.getPitch() <= -5f || Gdx.input.getRoll() <= -5f) {
			if (handInstances.size == 0) {
				return;
			}
			hand.destroyLastCard();
			handInstances.removeIndex(handInstances.size - 1);
			recalculateCardPositions();
		}

		ModelInstance hoveredCard = getIntersectingCard();
		if (hoveredCard != null) {
			hoveredCard.transform.translate(0f, CARD_HEIGHT * 0.2f, 0f);
			hoveredCard.calculateTransforms();
		}

		tweenManager.update(Gdx.graphics.getDeltaTime());

		modelBatch.begin(camera);
		modelBatch.render(handInstances, environment);
		modelBatch.render(deckInstances, environment);
		modelBatch.render(deck, environment);
		modelBatch.render(hand, environment);
		modelBatch.end();

		if (hoveredCard != null) {
			hoveredCard.transform.translate(0f, -CARD_HEIGHT * 0.2f, 0f);
			hoveredCard.calculateTransforms();
		}
	}

	private void recalculateCardPositions() {
		for (int i = 0; i < handInstances.size; i++) {
			ModelInstance instance = handInstances.get(i);
			float localX = (((-handInstances.size / 2f) + i) * (CARD_WIDTH * 0.1f)) + (CARD_WIDTH * 0.1f / 2f);
			float localZ = i * (CARD_DEPTH * 1.5f);
			float rotationDegrees = ((-(handInstances.size - 1) / 2f) + i) * -5f;
			instance.transform.idt();
			instance.transform.rotate(Vector3.Z, rotationDegrees);
			instance.transform.translate(localX, 0f, localZ);
			instance.calculateTransforms();
		}
	}

	private void recalculateDeckPositions() {
		for (int i = 0; i < deckInstances.size; i++) {
			ModelInstance instance = deckInstances.get(i);
			float localX = 3f;
			float localY = i * CARD_DEPTH;
			float localZ = -2f;
			instance.transform.idt();
			instance.transform.translate(localX, localY, localZ);
			instance.transform.rotate(Vector3.X, 90f);
			instance.transform.rotate(Vector3.Z, 180f);
			instance.calculateTransforms();
		}
	}

	private void addCardToHand() {
		Model usedModel = (handInstances.size % 2 == 0) ? redModel : blueModel;
		handInstances.add(new ModelInstance(usedModel));
	}

	private void addCardToDeck() {
		Model usedModel = (deckInstances.size % 2 == 0) ? redModel : blueModel;
		ModelInstance instance = new ModelInstance(usedModel);
		deckInstances.add(instance);

		Timeline timeline = Timeline.createParallel()
			.beginSequence()
			.push(Tween.to(instance, ModelInstanceAccessor.POSITION, 1f)
				.targetRelative(0f, 0f, -1f))
			.push(Tween.to(instance, ModelInstanceAccessor.POSITION, 1f)
				.targetRelative(0f, 1f, 0f))
			.push(Tween.to(instance, ModelInstanceAccessor.POSITION, 1f)
				.targetRelative(0f, 0f, 1f))
			.push(Tween.to(instance, ModelInstanceAccessor.POSITION, 1f)
				.targetRelative(0f, -1f, 0f))
			.end()
			.beginSequence()
			.push(Tween.to(instance, ModelInstanceAccessor.ROTATION_Y, 4f)
				.targetRelative(720f)
				.ease(Linear.INOUT))
			.end()
			.repeat(Tween.INFINITY, 0f);
		timeline.start(tweenManager);
	}

	private ModelInstance getIntersectingCard() {
		Ray mouseRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		ModelInstance closestInstance = null;
		float minDistance = Float.MAX_VALUE;
		for (ModelInstance instance : handInstances) {
			BoundingBox boundingBox = instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform);
			Vector3 intersection = new Vector3();
			if (Intersector.intersectRayBounds(mouseRay, boundingBox, intersection)) {
				float distanceSquared = camera.position.dst2(intersection);
				if (distanceSquared < minDistance) {
					minDistance = distanceSquared;
					closestInstance = instance;
				}
			}
		}

		return closestInstance;
	}

	@Override
	public void dispose() {
		super.dispose();

		cardTexture.dispose();

		modelBatch.dispose();
		redModel.dispose();
		blueModel.dispose();

		deck.dispose();
		hand.dispose();
	}

	private static class ModelInstanceAccessor implements TweenAccessor<ModelInstance> {
		private static final int POSITION = 1;
		private static final int ROTATION_X = 2;
		private static final int ROTATION_Y = 3;
		private static final int ROTATION_Z = 4;

		private Quaternion quaternionX;
		private Quaternion quaternionY;
		private Quaternion quaternionZ;

		@Override
		public int getValues(final ModelInstance target, final int tweenType, final float[] returnValues) {
			switch (tweenType) {
				case POSITION:
					Vector3 position = target.transform.getTranslation(new Vector3());
					returnValues[0] = position.x;
					returnValues[1] = position.y;
					returnValues[2] = position.z;
					return 3;
				case ROTATION_X:
					quaternionX = target.transform.getRotation(new Quaternion(), true);
					returnValues[0] = quaternionX.getAngleAround(Vector3.X);
					return 1;
				case ROTATION_Y:
					quaternionY = target.transform.getRotation(new Quaternion(), true);
					returnValues[0] = quaternionY.getAngleAround(Vector3.Y);
					return 1;
				case ROTATION_Z:
					quaternionZ = target.transform.getRotation(new Quaternion(), true);
					returnValues[0] = quaternionZ.getAngleAround(Vector3.Z);
					return 1;
				default:
					throw new IllegalArgumentException("Unknown tweenType: " + tweenType);
			}
		}

		@Override
		public void setValues(final ModelInstance target, final int tweenType, final float[] newValues) {
			switch (tweenType) {
				case POSITION:
					target.transform.setTranslation(newValues[0], newValues[1], newValues[2]);
					target.calculateTransforms();
					break;
				case ROTATION_X:
					Vector3 positionX = target.transform.getTranslation(new Vector3());
					target.transform.idt();
					target.transform.translate(positionX.x, positionX.y, positionX.z);
					target.transform.rotate(Vector3.X, newValues[0]);
					target.transform.rotate(quaternionX);
					target.calculateTransforms();
					break;
				case ROTATION_Y:
					Vector3 positionY = target.transform.getTranslation(new Vector3());
					target.transform.idt();
					target.transform.translate(positionY.x, positionY.y, positionY.z);
					target.transform.rotate(Vector3.Y, newValues[0]);
					target.transform.rotate(quaternionY);
					target.calculateTransforms();
					break;
				case ROTATION_Z:
					Vector3 positionZ = target.transform.getTranslation(new Vector3());
					target.transform.idt();
					target.transform.translate(positionZ.x, positionZ.y, positionZ.z);
					target.transform.rotate(Vector3.Z, newValues[0]);
					target.transform.rotate(quaternionZ);
					target.calculateTransforms();
					break;
				default:
					throw new IllegalArgumentException("Unknown tweenType: " + tweenType);
			}
		}
	}
}
