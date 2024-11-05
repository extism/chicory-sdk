package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;

import java.util.List;

public final class ExtismHostFunction {
    static final String DEFAULT_NAMESPACE = "extism:host/user";

    public static ExtismHostFunction of(
            String name,
            List<ValueType> paramTypes,
            List<ValueType> returnTypes,
            Handle handle) {
        return new ExtismHostFunction(DEFAULT_NAMESPACE, name, handle, paramTypes, returnTypes);
    }

    public static ExtismHostFunction of(
            String module,
            String name,
            Handle handle,
            List<ValueType> paramTypes,
            List<ValueType> returnTypes) {
        return new ExtismHostFunction(module, name, handle, paramTypes, returnTypes);
    }

    private final String module;
    private final String name;
    private final Handle handle;
    private final List<ValueType> paramTypes;
    private final List<ValueType> returnTypes;
    private CurrentPlugin currentPlugin;

    ExtismHostFunction(
            String module,
            String name,
            Handle handle,
            List<ValueType> paramTypes,
            List<ValueType> returnTypes) {
        this.module = module;
        this.name = name;
        this.handle = handle;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public void bind(CurrentPlugin p) {
        if (currentPlugin != null) {
            throw new IllegalArgumentException(
                    String.format("Function '%s.%s' is already bound to %s.",
                            module, name, currentPlugin));
        }
        this.currentPlugin = p;
    }

    final HostFunction asHostFunction() {
        return new HostFunction(
                module, name, paramTypes, returnTypes,
                (Instance inst, long... args) -> handle.apply(this.currentPlugin, args));
    }

    @FunctionalInterface
    public interface Handle {
        long[] apply(CurrentPlugin currentPlugin, long... args);
    }
}
