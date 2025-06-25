package org.extism.sdk.chicory;

import com.dylibso.chicory.wasm.types.Value;

public class ExtismValueList {
    private final ExtismValType[] types;
    private final long[] values;

    ExtismValueList(ExtismValType[] types, long[] values) {
        this.types = types;
        this.values = values;
    }

    public int getInt(int i) {
        assertType(ExtismValType.I32, types[i]);
        return (int) values[i];
    }

    public void setInt(int i, int v) {
        assertType(ExtismValType.I32, types[i]);
        values[i] = v;
    }

    public long getLong(int i) {
        assertType(ExtismValType.I64, types[i]);
        return values[i];
    }

    public void setLong(int i, long v) {
        assertType(ExtismValType.I64, types[i]);
        values[i] = v;
    }

    public float getFloat(int i) {
        assertType(ExtismValType.F32, types[i]);
        return Value.longToFloat(values[i]);
    }

    public void setFloat(int i, float v) {
        assertType(ExtismValType.F32, types[i]);
        values[i] = Value.floatToLong(v);
    }

    public double getDouble(int i) {
        assertType(ExtismValType.F64, types[i]);
        return Value.longToDouble(values[i]);
    }

    public void setDouble(int i, double v) {
        assertType(ExtismValType.F64, types[i]);
        values[i] = Value.doubleToLong(v);
    }

    public long getRaw(int i) {
        return values[i];
    }

    public void setRaw(int i, long value) {
        values[i] = value;
    }


    long[] unwrap() {
        return values;
    }

    private void assertType(ExtismValType expected, ExtismValType given) {
        if (given != expected) {
            throw new ExtismTypeConversionException(expected, given);
        }
    }

}
