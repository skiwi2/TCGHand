package com.github.skiwi2.tcghand;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class TCGHandGame extends ApplicationAdapter {
	private static final float CARD_WIDTH = 1f;
	private static final float CARD_HEIGHT = 1.5f;
	private static final float CARD_DEPTH = 0.01f;

	private PerspectiveCamera camera;
	private CameraInputController cameraInputController;

	private Texture cardTexture;

	private ModelBatch modelBatch;
	private Model redModel;
	private Model blueModel;
	private Array<ModelInstance> handInstances = new Array<ModelInstance>();
	private Array<ModelInstance> deckInstances = new Array<ModelInstance>();

	private Environment environment;

	@Override
	public void create() {
		super.create();

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
	}

	@Override
	public void render() {
		super.render();

		Gdx.gl.glClearColor(0.2f, 0f, 0f, 1f);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.getPitch() >= 5f || Gdx.input.getRoll() >= 5f) {
			addCardToHand();
			recalculateCardPositions();
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.getPitch() <= -5f || Gdx.input.getRoll() <= -5f) {
			if (handInstances.size == 0) {
				return;
			}
			handInstances.removeIndex(handInstances.size - 1);
			recalculateCardPositions();
		}

		ModelInstance hoveredCard = getIntersectingCard();
		if (hoveredCard != null) {
			hoveredCard.transform.translate(0f, CARD_HEIGHT * 0.2f, 0f);
			hoveredCard.calculateTransforms();
		}

		modelBatch.begin(camera);
		modelBatch.render(handInstances, environment);
		modelBatch.render(deckInstances, environment);
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
		deckInstances.add(new ModelInstance(usedModel));
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
	}
}
