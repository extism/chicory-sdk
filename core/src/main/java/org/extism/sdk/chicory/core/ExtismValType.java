package org.extism.sdk.chicory.core;

import com.dylibso.chicory.wasm.types.ValType;

public enum ExtismValType {
    I32(ValType.I32),
    I64(ValType.I64),
    F32(ValType.F32),
    F64(ValType.F64);

    private final ValType chicoryType;

    ExtismValType(ValType chicoryType) {
        this.chicoryType = chicoryType;
    }

    ValType toChicoryValueType() {
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
