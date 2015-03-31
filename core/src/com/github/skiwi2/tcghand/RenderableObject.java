package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * @author Frank van Heeswijk
 */
public abstract class RenderableObject implements RenderableProvider, Disposable {
    public Matrix4 transform = new Matrix4();

    protected final Array<ModelInstance> instances = new Array<ModelInstance>();

    protected final TweenManager tweenManager;
    protected final TransitioningZone transitioningZone;

    public RenderableObject(final TweenManager tweenManager, final TransitioningZone transitioningZone) {
        this.tweenManager = tweenManager;
        this.transitioningZone = transitioningZone;
    }

    public boolean isEmpty() {
        return (instances.size == 0);
    }

    @Override
    public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
        for (ModelInstance instance : instances) {
            instance.getRenderables(renderables, pool);
        }
    }
}
