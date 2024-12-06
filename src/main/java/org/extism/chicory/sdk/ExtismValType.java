package org.extism.chicory.sdk;

import com.dylibso.chicory.wasm.types.ValueType;

public enum ExtismValType {
    I32(ValueType.I32),
    I64(ValueType.I64),
    F32(ValueType.F32),
    F64(ValueType.F64);

    private final ValueType chicoryType;

    ExtismValType(ValueType chicoryType) {
        this.chicoryType = chicoryType;
    }

    ValueType toChicoryValueType() {
        return chicoryType;
    }

    public ExtismValue toExtismValue(long v) {
        switch (this) {
            case I32:
                return ExtismValue.i32(v);
            case I64:
                return ExtismValue.i64(v);
            case F32:
                return ExtismValue.f32FromLongBits(v);
            case F64:
                return ExtismValue.f64FromLongBits(v);
            default:
                throw new IllegalArgumentException();
        }
    }

    public long toChicoryValue(ExtismValue value) {
        return 0;
    }
}
