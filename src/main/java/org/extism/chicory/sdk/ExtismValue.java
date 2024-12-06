package org.extism.chicory.sdk;

import com.dylibso.chicory.wasm.types.Value;

public class ExtismValue {

    public static ExtismValue i32(int data) {
        return i32((long) data);
    }

    public static ExtismValue i32(long data) {
        return new ExtismValue(ExtismValType.I32, data);
    }

    public static ExtismValue i64(long data) {
        return new ExtismValue(ExtismValType.I64, data);
    }

    public static ExtismValue f32FromLongBits(long data) {
        return new ExtismValue(ExtismValType.F32, data);
    }

    public static ExtismValue f32(float data) {
        return new ExtismValue(ExtismValType.F32, Value.floatToLong(data));
    }

    public static ExtismValue f64(double data) {
        return new ExtismValue(ExtismValType.F32, Value.doubleToLong(data));
    }

    public static ExtismValue f64FromLongBits(long data) {
        return new ExtismValue(ExtismValType.F32, data);
    }

    private final Value chicoryValue;

    private ExtismValue(ExtismValType valType, long value) {
        this.chicoryValue = new Value(valType.toChicoryValueType(), value);
    }



    long unwrap() {
        return chicoryValue.raw();
    }

}
