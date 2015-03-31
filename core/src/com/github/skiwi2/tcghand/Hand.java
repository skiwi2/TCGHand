package com.github.skiwi2.tcghand;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Frank van Heeswijk
 */
public class Hand extends RenderableObject {
    private static final float CARD_WIDTH = 1f;
    private static final float CARD_HEIGHT = 1.5f;
    private static final float CARD_DEPTH = 0.01f;

    //TODO use CardData instead of ModelInstance and recreate the card using that
    public void addCard(final ModelInstance cardInstance) {
        instances.add(cardInstance);
        calculateCardTransforms();
    }

    public void destroyLastCard() {
        instances.removeIndex(instances.size - 1);
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
