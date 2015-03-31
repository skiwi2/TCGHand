package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

/**
 * @author Frank van Heeswijk
 */
public class TransitioningZone extends RenderableObject {
    public TransitioningZone(final TweenManager tweenManager) {
        super(tweenManager, null);
    }

    public void addCard(final ModelInstance modelInstance) {
        instances.add(modelInstance);
    }

    public void removeCard(final ModelInstance modelInstance) {
        instances.removeValue(modelInstance, true);
    }

    @Override
    public void dispose() {

    }
}
