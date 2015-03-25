package com.github.skiwi2.tcghand;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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

	private ModelBatch modelBatch;
	private Model redModel;
	private Model blueModel;
	private Array<ModelInstance> instances = new Array<ModelInstance>();

	private Environment environment;

	@Override
	public void create () {
		super.create();

		camera = new PerspectiveCamera(60f, 800, 600);
		camera.position.set(0f, 1f, 3f);
		camera.lookAt(0f, 0f, 0f);
		camera.near = 0.1f;
		camera.far = 1000f;
		camera.update();
		cameraInputController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraInputController);

		modelBatch = new ModelBatch();
		redModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
			new Material(ColorAttribute.createDiffuse(Color.RED)),
			Usage.Position | Usage.Normal);
		blueModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
			new Material(ColorAttribute.createDiffuse(Color.BLUE)),
			Usage.Position | Usage.Normal);
		addCard();
		recalculateCardPositions();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	@Override
	public void render () {
		super.render();

		Gdx.gl.glClearColor(0.2f, 0f, 0f, 1f);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			addCard();
			recalculateCardPositions();
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			if (instances.size == 0) {
				return;
			}
			instances.removeIndex(instances.size - 1);
			recalculateCardPositions();
		}

		ModelInstance hoveredCard = getIntersectingCard();
		if (hoveredCard != null) {
			hoveredCard.transform.translate(0f, CARD_HEIGHT * 0.2f, 0f);
			hoveredCard.calculateTransforms();
		}

		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();

		if (hoveredCard != null) {
			hoveredCard.transform.translate(0f, -CARD_HEIGHT * 0.2f, 0f);
			hoveredCard.calculateTransforms();
		}
	}

	private void recalculateCardPositions() {
		for (int i = 0; i < instances.size; i++) {
			ModelInstance instance = instances.get(i);
			float localX = (((-instances.size / 2f) + i) * (CARD_WIDTH * 0.1f)) + (CARD_WIDTH * 0.1f / 2f);
			float localZ = i * (CARD_DEPTH * 1.5f);
			float rotationDegrees = ((-(instances.size - 1) / 2f) + i) * -5f;
			instance.transform.idt();
			instance.transform.rotate(Vector3.Z, rotationDegrees);
			instance.transform.translate(localX, 0f, localZ);
			instance.calculateTransforms();
		}
	}

	private void addCard() {
		Model usedModel = (instances.size % 2 == 0) ? redModel : blueModel;
		instances.add(new ModelInstance(usedModel));
	}

	private ModelInstance getIntersectingCard() {
		Ray mouseRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		ModelInstance closestInstance = null;
		float maxZ = Float.MIN_VALUE;
		for (ModelInstance instance : instances) {
			BoundingBox boundingBox = instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform);
			Vector3 intersection = new Vector3();
			if (Intersector.intersectRayBounds(mouseRay, boundingBox, intersection)) {
				if (intersection.z > maxZ) {
					maxZ = intersection.z;
					closestInstance = instance;
				}
			}
		}

		return closestInstance;
	}

	@Override
	public void dispose() {
		super.dispose();

		modelBatch.dispose();
		redModel.dispose();
	}
}
