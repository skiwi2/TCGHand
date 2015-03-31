package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Frank van Heeswijk
 */
public class Hand extends RenderableObject {
    private static final float CARD_WIDTH = 1f;
    private static final float CARD_HEIGHT = 1.5f;
    private static final float CARD_DEPTH = 0.01f;

    public Hand(final TweenManager tweenManager, final TransitioningZone transitioningZone) {
        super(tweenManager, transitioningZone);
    }

    //TODO use CardData instead of ModelInstance and recreate the card using that
    public void addCard(final ModelInstance cardInstance) {
        Matrix4 targetTransform = getTargetTransform().mulLeft(transform);
        Vector3 targetVector = targetTransform.getTranslation(new Vector3());
        float targetAngle = targetTransform.getRotation(new Quaternion(), true).getAngleAround(Vector3.Z);

        Timeline timeline = Timeline.createParallel();
        for (ModelInstance instance : instances) {
//            timeline.push(Tween.to(instance, ModelInstanceAccessor.ROTATION_Z, 1f)
//                .targetRelative(5f));
        }
        timeline.beginSequence()
            .push(Tween.to(cardInstance, ModelInstanceAccessor.POSITION, 0.5f)
                //TODO should be (0, 0, alpha) instead of (0, -alpha, 0)? only because deck is by default rotated
                .targetRelative(0f, -(transform.getTranslation(new Vector3()).z - cardInstance.transform.getTranslation(new Vector3()).z), 0f))
            .beginParallel()
                .push(Tween.to(cardInstance, ModelInstanceAccessor.ROTATION_Z, 0.5f)
                    .targetRelative(180f))
//                .push(Tween.to(cardInstance, ModelInstanceAccessor.ROTATION_X, 0.5f)
//                    .targetRelative(90f - targetAngle))
//                .push(Tween.to(cardInstance, ModelInstanceAccessor.POSITION, 0.5f)
//                    .target(targetVector.x, targetVector.y, targetVector.z))
            .end()
            .push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(final int type, final BaseTween<?> source) {
                    transitioningZone.removeCard(cardInstance);
                    instances.add(cardInstance);
                }
            }))
        .end();

        timeline.start(tweenManager);
    }

    public void destroyLastCard() {
        instances.removeIndex(instances.size - 1);
        calculateCardTransforms();

        //TODO here also rotate all cards to correct angle
    }

    private Matrix4 getTargetTransform() {
        Matrix4 targetTransform = new Matrix4();
        int i = instances.size;
        float localX = (((-instances.size / 2f) + i) * (CARD_WIDTH * 0.1f)) + (CARD_WIDTH * 0.1f / 2f);
        float localZ = i * (CARD_DEPTH * 1.5f);
        float rotationDegrees = ((-(instances.size - 1) / 2f) + i) * -5f;
        targetTransform.rotate(Vector3.Z, rotationDegrees);
        targetTransform.translate(localX, 0f, localZ);
        return targetTransform;
    }

    private void calculateCardTransforms() {
        for (int i = 0; i < instances.size; i++) {
            ModelInstance cardInstance = instances.get(i);
            float localX = (((-instances.size / 2f) + i) * (CARD_WIDTH * 0.1f)) + (CARD_WIDTH * 0.1f / 2f);
            float localZ = i * (CARD_DEPTH * 1.5f);
            float rotationDegrees = ((-(instances.size - 1) / 2f) + i) * -5f;
            cardInstance.transform.idt();
            cardInstance.transform.rotate(Vector3.Z, rotationDegrees);
            cardInstance.transform.translate(localX, 0f, localZ);
            cardInstance.calculateTransforms();
        }
    }

    @Override
    public void dispose() {

    }
}
