package com.github.skiwi2.tcghand;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * @author Frank van Heeswijk
 */
public class Deck implements RenderableProvider, Disposable {
    private static final float CARD_WIDTH = 1f;
    private static final float CARD_HEIGHT = 1.5f;
    private static final float CARD_DEPTH = 0.01f;

    public Matrix4 transform = new Matrix4();

    private final Matrix4 deckTransform = new Matrix4().rotate(Vector3.X, 90f).rotate(Vector3.Z, 180f);

    private final Array<ModelInstance> cardInstances = new Array<ModelInstance>();
    private final Array<Disposable> usedDisposables = new Array<Disposable>();

    public void addCard(final Texture texture) {
        Model cardModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
            new Material(ColorAttribute.createDiffuse(Color.RED), TextureAttribute.createDiffuse(texture)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        ModelInstance cardInstance = new ModelInstance(cardModel);
        cardInstances.add(cardInstance);

        cardInstance.transform.setToTranslation(0f, 0f, -cardInstances.size * CARD_DEPTH);

        usedDisposables.add(cardModel);
    }

    @Override
    public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
        for (ModelInstance cardInstance : cardInstances) {
            Array<Renderable> localRenderables = new Array<Renderable>();
            cardInstance.getRenderables(localRenderables, pool);
            for (Renderable localRenderable : localRenderables) {
                localRenderable.worldTransform.mulLeft(deckTransform).mulLeft(transform);
                renderables.add(localRenderable);
            }
        }
    }

    @Override
    public void dispose() {
        for (Disposable disposable : usedDisposables) {
            disposable.dispose();
        }
    }
}
