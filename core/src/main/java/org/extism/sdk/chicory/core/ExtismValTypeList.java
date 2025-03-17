package org.extism.sdk.chicory.core;

import com.dylibso.chicory.wasm.types.ValType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ExtismValTypeList {
    private final ExtismValType[] types;
    private final List<ValType> chicoryTypes;

    ExtismValTypeList(List<ExtismValType> types) {
        this.types = types.toArray(ExtismValType[]::new);
        this.chicoryTypes = types.stream().map(ExtismValType::toChicoryValueType)
                .collect(Collectors.toList());
    }

    List<ValType> toChicoryTypes() {
        return Arrays.stream(types)
                .map(ExtismValType::toChicoryValueType)
                .collect(Collectors.toList());
    }

    public ExtismValueList toExtismValueList(long[] args) {
        return new ExtismValueList(this.types, args);
    }

    public ExtismValueList toExtismValueList() {
        return new ExtismValueList(this.types, new long[this.types.length]);
    }

}
