package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Frank van Heeswijk
 */
public class Deck extends RenderableObject {
    private static final float CARD_WIDTH = 1f;
    private static final float CARD_HEIGHT = 1.5f;
    private static final float CARD_DEPTH = 0.01f;

    private final Matrix4 deckTransform = new Matrix4().rotate(Vector3.X, 90f).rotate(Vector3.Z, 180f);

    private final Array<Disposable> usedDisposables = new Array<Disposable>();

    public Deck(final TweenManager tweenManager, final TransitioningZone transitioningZone) {
        super(tweenManager, transitioningZone);
    }

    public void addCard(final Texture texture) {
        Color color = (instances.size % 2 == 0) ? Color.RED : Color.BLUE;
        Model cardModel = new ModelBuilder().createBox(CARD_WIDTH, CARD_HEIGHT, CARD_DEPTH,
            new Material(ColorAttribute.createDiffuse(color), TextureAttribute.createDiffuse(texture)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        ModelInstance cardInstance = new ModelInstance(cardModel);
        cardInstance.transform.translate(0f, 0f, -instances.size * CARD_DEPTH);
        cardInstance.transform.mulLeft(deckTransform).mulLeft(transform);
        instances.add(cardInstance);

        usedDisposables.add(cardModel);
    }

    public ModelInstance drawCard() {
        ModelInstance instance = instances.removeIndex(instances.size - 1);
        transitioningZone.addCard(instance);
        return instance;
    }

    @Override
    public void dispose() {
        for (Disposable disposable : usedDisposables) {
            disposable.dispose();
        }
    }
}
