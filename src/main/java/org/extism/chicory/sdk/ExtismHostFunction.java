package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

import java.util.List;

public final class ExtismHostFunction {
    static final String DEFAULT_NAMESPACE = "extism:host/user";

    public static ExtismHostFunction of(
            String name,
            List<ExtismValType> paramTypes,
            List<ExtismValType> returnTypes,
            ExtismFunction extismFunction) {
        return new ExtismHostFunction(DEFAULT_NAMESPACE, name, paramTypes, returnTypes, extismFunction);
    }

    public static ExtismHostFunction of(
            String module,
            String name,
            ExtismFunction extismFunction,
            List<ExtismValType> paramTypes,
            List<ExtismValType> returnTypes) {
        return new ExtismHostFunction(module, name, paramTypes, returnTypes, extismFunction);
    }

    private final String module;
    private final String name;
    private final ExtismFunction extismFunction;
    private final ExtismValTypeList paramTypes;
    private final ExtismValTypeList returnTypes;
    private CurrentPlugin currentPlugin;

    private ExtismHostFunction(
            String module,
            String name,
            List<ExtismValType> paramTypes,
            List<ExtismValType> returnTypes,
            ExtismFunction extismFunction) {
        this.module = module;
        this.name = name;
        this.paramTypes = new ExtismValTypeList(paramTypes);
        this.returnTypes = new ExtismValTypeList(returnTypes);
        this.extismFunction = extismFunction;
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
                module, name, paramTypes.toChicoryTypes(), returnTypes.toChicoryTypes(),
                (Instance inst, long... args) -> {
                    var params = paramTypes.toExtismValueList(args);
                    var results = returnTypes.toExtismValueList();
                    extismFunction.apply(this.currentPlugin, params, results);
                    return results.unwrap();
                });
    }

}
