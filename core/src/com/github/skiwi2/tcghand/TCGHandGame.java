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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

public class TCGHandGame extends ApplicationAdapter {
	private static final float CARD_WIDTH = 1f;
	private static final float CARD_HEIGHT = 1.5f;
	private static final float CARD_DEPTH = 0.05f;

	private PerspectiveCamera camera;

	private ModelBatch modelBatch;
	private Model model;
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

		modelBatch = new ModelBatch();
		model = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
			new Material(ColorAttribute.createDiffuse(Color.RED)),
			Usage.Position | Usage.Normal);
		instances.add(new ModelInstance(model));

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
			instances.add(new ModelInstance(model));
			recalculateInstancePositions();
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			if (instances.size == 0) {
				return;
			}
			instances.removeIndex(instances.size - 1);
			recalculateInstancePositions();
		}

		modelBatch.begin(camera);
		for (ModelInstance instance : instances) {
			modelBatch.render(instance, environment);
		}
		modelBatch.end();
	}

	private void recalculateInstancePositions() {
		for (int i = 0; i < instances.size; i++) {
			ModelInstance instance = instances.get(i);
			float localX = (((-instances.size / 2f) + i) * (CARD_WIDTH * 1.1f)) + (CARD_WIDTH / 2f);
			float localZ = i * (CARD_DEPTH / 2f);
			instance.transform.setToTranslation(localX, 0f, localZ);
			instance.calculateTransforms();
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		modelBatch.dispose();
		model.dispose();
	}
}
