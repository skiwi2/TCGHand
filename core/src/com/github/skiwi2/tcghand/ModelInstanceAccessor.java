package com.github.skiwi2.tcghand;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;

/**
 * @author Frank van Heeswijk
 */
public class ModelInstanceAccessor extends TweenDeltaAccessor<ModelInstance> {
    public static final int POSITION = 1;
    public static final int ROTATION_X = 2;
    public static final int ROTATION_Y = 3;
    public static final int ROTATION_Z = 4;

    private Quaternion quaternionX;
    private Quaternion quaternionY;
    private Quaternion quaternionZ;

    @Override
    public int getValuesInternal(final ModelInstance target, final int tweenType, final float[] returnValues) {
        switch (tweenType) {
            case POSITION:
                Vector3 position = target.transform.getTranslation(new Vector3());
                returnValues[0] = position.x;
                returnValues[1] = position.y;
                returnValues[2] = position.z;
                return 3;
            case ROTATION_X:
                quaternionX = target.transform.getRotation(new Quaternion(), true);
                returnValues[0] = quaternionX.getAngleAround(Vector3.X);
                return 1;
            case ROTATION_Y:
                quaternionY = target.transform.getRotation(new Quaternion(), true);
                returnValues[0] = quaternionY.getAngleAround(Vector3.Y);
                return 1;
            case ROTATION_Z:
                quaternionZ = target.transform.getRotation(new Quaternion(), true);
                returnValues[0] = quaternionZ.getAngleAround(Vector3.Z);
                return 1;
            default:
                throw new IllegalArgumentException("Unknown tweenType: " + tweenType);
        }
    }

    @Override
    public void updateValues(final ModelInstance target, final int tweenType, final float[] deltaValues) {
        switch (tweenType) {
            case POSITION:
                target.transform.translate(deltaValues[0], deltaValues[1], deltaValues[2]);
                target.calculateTransforms();
                break;
            case ROTATION_X:
                Vector3 positionX = target.transform.getTranslation(new Vector3());
                //TODO scale is not stored?
                target.transform.idt();
                target.transform.translate(positionX.x, positionX.y, positionX.z);
                target.transform.rotate(Vector3.X, deltaValues[0]);
                target.transform.rotate(quaternionX);
                target.calculateTransforms();
                break;
            case ROTATION_Y:
                Vector3 positionY = target.transform.getTranslation(new Vector3());
                //TODO scale is not stored?
                target.transform.idt();
                target.transform.translate(positionY.x, positionY.y, positionY.z);
                target.transform.rotate(Vector3.Y, deltaValues[0]);
                target.transform.rotate(quaternionY);
                target.calculateTransforms();
                break;
            case ROTATION_Z:
                Vector3 positionZ = target.transform.getTranslation(new Vector3());
//                target.transform.idt();
                target.transform.translate(positionZ.x, positionZ.y, positionZ.z);
                target.transform.rotate(Vector3.Z, deltaValues[0]);
                target.transform.translate(-positionZ.x, -positionZ.y, -positionZ.z);
                //TODO fix this, only apply relative rotation in the end
//                target.transform.rotate(quaternionZ);
                target.calculateTransforms();
                break;
            default:
                throw new IllegalArgumentException("Unknown tweenType: " + tweenType);
        }
    }
}