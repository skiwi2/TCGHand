package com.github.skiwi2.tcghand;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.utils.Pool;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frank van Heeswijk
 */
public abstract class TweenDeltaAccessor<T> implements TweenAccessor<T> {
    private final Pool<Record> recordPool = new Pool<Record>() {
        @Override
        protected Record newObject() {
            return new Record();
        }
    };

    private final Map<Record, float[]> lastValuesMap = new HashMap<Record, float[]>();
    private final Map<Record, float[]> deltaValuesMap = new HashMap<Record, float[]>();
    private final Map<Record, int[]> numberOfAttributesMap = new HashMap<Record, int[]>();

    public void freeMemory() {
        lastValuesMap.clear();
        deltaValuesMap.clear();
        numberOfAttributesMap.clear();
    }

    @Override
    public int getValues(final T target, final int tweenType, final float[] returnValues) {
        int numberOfAttributes = getValuesInternal(target, tweenType, returnValues);
        getNumberOfAttributesForRecord(target, tweenType)[0] = numberOfAttributes;
        float[] lastValues = getLastValuesForRecord(target, tweenType);
        System.arraycopy(returnValues, 0, lastValues, 0, numberOfAttributes);
        return numberOfAttributes;
    }

    @Override
    public void setValues(final T target, final int tweenType, final float[] newValues) {
        int numberOfAttributes = getNumberOfAttributesForRecord(target, tweenType)[0];
        float[] deltaValues = getDeltaValuesForRecord(target, tweenType);
        float[] lastValues = getLastValuesForRecord(target, tweenType);
        for (int i = 0; i < numberOfAttributes; i++) {
            deltaValues[i] = newValues[i] - lastValues[i];
        }
        updateValues(target, tweenType, deltaValues);
        System.arraycopy(newValues, 0, lastValues, 0, numberOfAttributes);
    }

    protected abstract int getValuesInternal(final T target, final int tweenType, final float[] returnValues);

    protected abstract void updateValues(final T target, final int tweenType, final float[] deltaValues);

    private float[] getLastValuesForRecord(final T target, final int tweenType) {
        Record record = recordPool.obtain();
        record.target = target;
        record.tweenType = tweenType;
        if (!lastValuesMap.containsKey(record)) {
            lastValuesMap.put(record, new float[TCGHandGame.TWEEN_COMBINED_ATTRIBUTES_LIMIT]);
        }
        float[] lastValues = lastValuesMap.get(record);
        recordPool.free(record);
        return lastValues;
    }

    private float[] getDeltaValuesForRecord(final T target, final int tweenType) {
        Record record = recordPool.obtain();
        record.target = target;
        record.tweenType = tweenType;
        if (!deltaValuesMap.containsKey(record)) {
            deltaValuesMap.put(record, new float[TCGHandGame.TWEEN_COMBINED_ATTRIBUTES_LIMIT]);
        }
        float[] deltaValues = deltaValuesMap.get(record);
        recordPool.free(record);
        return deltaValues;
    }

    private int[] getNumberOfAttributesForRecord(final T target, final int tweenType) {
        Record record = recordPool.obtain();
        record.target = target;
        record.tweenType = tweenType;
        if (!numberOfAttributesMap.containsKey(record)) {
            numberOfAttributesMap.put(record, new int[1]);
        }
        int[] numberOfAttributes = numberOfAttributesMap.get(record);
        recordPool.free(record);
        return numberOfAttributes;
    }

    private class Record implements Pool.Poolable {
        private T target;
        private int tweenType;

        @Override
        public void reset() {
            target = null;
            tweenType = 0;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Record record = (Record)obj;

            if (tweenType != record.tweenType) {
                return false;
            }
            if (target != null ? !target.equals(record.target) : record.target != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = target != null ? target.hashCode() : 0;
            result = 31 * result + tweenType;
            return result;
        }
    }
}
