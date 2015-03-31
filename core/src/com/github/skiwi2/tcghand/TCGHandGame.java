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
	private TweenManager tweenManager;

	private PerspectiveCamera camera;
	private CameraInputController cameraInputController;

	private Texture cardTexture;

	private ModelBatch modelBatch;

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

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		deck = new Deck();
		for (int i = 0; i < 60; i++) {
			deck.addCard(cardTexture);
		}
		deck.transform.translate(3f, 0f, -2f);

		hand = new Hand();
	}

	@Override
	public void render() {
		super.render();

		Gdx.gl.glClearColor(0.2f, 0f, 0f, 1f);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.getPitch() >= 5f || Gdx.input.getRoll() >= 5f) {
			if (deck.isEmpty()) {
				return;
			}
			hand.addCard(deck.drawCard());
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.getPitch() <= -5f || Gdx.input.getRoll() <= -5f) {
			if (hand.isEmpty()) {
				return;
			}
			hand.destroyLastCard();
		}

//		ModelInstance hoveredCard = getIntersectingCard();
//		if (hoveredCard != null) {
//			hoveredCard.transform.translate(0f, CARD_HEIGHT * 0.2f, 0f);
//			hoveredCard.calculateTransforms();
//		}

		tweenManager.update(Gdx.graphics.getDeltaTime());

		modelBatch.begin(camera);
		modelBatch.render(deck, environment);
		modelBatch.render(hand, environment);
		modelBatch.end();

//		if (hoveredCard != null) {
//			hoveredCard.transform.translate(0f, -CARD_HEIGHT * 0.2f, 0f);
//			hoveredCard.calculateTransforms();
//		}
	}

//	private ModelInstance getIntersectingCard() {
//		Ray mouseRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
//
//		ModelInstance closestInstance = null;
//		float minDistance = Float.MAX_VALUE;
//		for (ModelInstance instance : handInstances) {
//			BoundingBox boundingBox = instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform);
//			Vector3 intersection = new Vector3();
//			if (Intersector.intersectRayBounds(mouseRay, boundingBox, intersection)) {
//				float distanceSquared = camera.position.dst2(intersection);
//				if (distanceSquared < minDistance) {
//					minDistance = distanceSquared;
//					closestInstance = instance;
//				}
//			}
//		}
//
//		return closestInstance;
//	}

	@Override
	public void dispose() {
		super.dispose();

		cardTexture.dispose();

		modelBatch.dispose();

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
