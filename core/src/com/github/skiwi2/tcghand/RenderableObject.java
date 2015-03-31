package com.github.skiwi2.tcghand;

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

    private final Array<Matrix4> extraTransforms = new Array<Matrix4>();

    public void addExtraTransform(final Matrix4 extraTransform) {
        this.extraTransforms.add(extraTransform);
    }

    public boolean isEmpty() {
        return (instances.size == 0);
    }

    @Override
    public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
        for (ModelInstance instance : instances) {
            Array<Renderable> localRenderables = new Array<Renderable>();
            instance.getRenderables(localRenderables, pool);
            for (Renderable localRenderable : localRenderables) {
                for (Matrix4 extraTransform : extraTransforms) {
                    localRenderable.worldTransform.mulLeft(extraTransform);
                }
                localRenderable.worldTransform.mulLeft(transform);
                renderables.add(localRenderable);
            }
        }
    }
}
