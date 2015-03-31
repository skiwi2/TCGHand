package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;

public class TCGHandGame extends ApplicationAdapter {
	public static final int TWEEN_COMBINED_ATTRIBUTES_LIMIT = 3;

	private TweenManager tweenManager;
	private TweenDeltaAccessor<ModelInstance> tweenDeltaAccessor;

	private PerspectiveCamera camera;
	private CameraInputController cameraInputController;

	private Texture cardTexture;

	private ModelBatch modelBatch;

	private final Array<RenderableObject> renderableObjects = new Array<RenderableObject>();

	private TransitioningZone transitioningZone;
	private Deck deck;
	private Hand hand;

	private Environment environment;

	@Override
	public void create() {
		super.create();

		Tween.setCombinedAttributesLimit(TWEEN_COMBINED_ATTRIBUTES_LIMIT);
		tweenDeltaAccessor = new ModelInstanceAccessor();
		Tween.registerAccessor(ModelInstance.class, tweenDeltaAccessor);
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

		transitioningZone = new TransitioningZone(tweenManager);

		deck = new Deck(tweenManager, transitioningZone);
		deck.transform.translate(3f, 0f, -2f);
		for (int i = 0; i < 60; i++) {
			deck.addCard(cardTexture);
		}

		hand = new Hand(tweenManager, transitioningZone);

		renderableObjects.addAll(transitioningZone, deck, hand);
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
		if (tweenManager.size() == 0) {
			tweenDeltaAccessor.freeMemory();
		}

		modelBatch.begin(camera);
		modelBatch.render(renderableObjects, environment);
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

		for (RenderableObject renderableObject : renderableObjects) {
			renderableObject.dispose();
		}
	}
}
