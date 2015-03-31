package com.github.skiwi2.tcghand;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * @author Frank van Heeswijk
 */
public class Hand implements RenderableProvider, Disposable {
    private static final float CARD_WIDTH = 1f;
    private static final float CARD_HEIGHT = 1.5f;
    private static final float CARD_DEPTH = 0.01f;

    public Matrix4 transform = new Matrix4();

    private final Array<ModelInstance> cardInstances = new Array<ModelInstance>();

    //TODO use CardData instead of ModelInstance and recreate the card using that
    public void addCard(final ModelInstance cardInstance) {
        cardInstances.add(cardInstance);
        calculateCardTransforms();
    }

    public void destroyLastCard() {
        cardInstances.removeIndex(cardInstances.size - 1);
    }

    public boolean isEmpty() {
        return (cardInstances.size == 0);
    }

    private void calculateCardTransforms() {
        for (int i = 0; i < cardInstances.size; i++) {
            ModelInstance cardInstance = cardInstances.get(i);
            float localX = (((-cardInstances.size / 2f) + i) * (CARD_WIDTH * 0.1f)) + (CARD_WIDTH * 0.1f / 2f);
            float localZ = i * (CARD_DEPTH * 1.5f);
            float rotationDegrees = ((-(cardInstances.size - 1) / 2f) + i) * -5f;
            cardInstance.transform.idt();
            cardInstance.transform.rotate(Vector3.Z, rotationDegrees);
            cardInstance.transform.translate(localX, 0f, localZ);
            cardInstance.calculateTransforms();
        }
    }

    @Override
    public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
        for (ModelInstance cardInstance : cardInstances) {
            Array<Renderable> localRenderables = new Array<Renderable>();
            cardInstance.getRenderables(localRenderables, pool);
            for (Renderable localRenderable : localRenderables) {
                localRenderable.worldTransform.mulLeft(transform);
                renderables.add(localRenderable);
            }
        }
    }

    @Override
    public void dispose() {

    }
}
