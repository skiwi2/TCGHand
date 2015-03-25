package com.github.skiwi2.tcghand;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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

public class TCGHandGame extends ApplicationAdapter {
	private PerspectiveCamera camera;

	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;

	private Environment environment;

	@Override
	public void create () {
		super.create();

		camera = new PerspectiveCamera(60f, 800, 600);
		camera.position.set(0f, 10f, 10f);
		camera.lookAt(0f, 0f, 0f);
		camera.near = 0.1f;
		camera.far = 1000f;
		camera.update();

		modelBatch = new ModelBatch();
		model = new ModelBuilder().createBox(1f, 2f, 01f,
			new Material(ColorAttribute.createDiffuse(Color.RED)),
			Usage.Position | Usage.Normal);
		instance = new ModelInstance(model);

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

		modelBatch.begin(camera);
		modelBatch.render(instance, environment);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		super.dispose();

		modelBatch.dispose();
		model.dispose();
	}
}
