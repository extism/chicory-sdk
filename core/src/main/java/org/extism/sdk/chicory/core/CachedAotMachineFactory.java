package org.extism.sdk.chicory.core;

import com.dylibso.chicory.experimental.aot.AotMachineFactory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasm.WasmModule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class CachedAotMachineFactory implements Function<Instance, Machine> {
    private final Map<WasmModule, AotMachineFactory> factories = new HashMap<>();

    public CachedAotMachineFactory compile(WasmModule wasmModule) {
        if (!factories.containsKey(wasmModule)) {
            factories.put(wasmModule, new AotMachineFactory(wasmModule));
        }
        return this;
    }

    public Machine apply(Instance instance) {
        if (!factories.containsKey(instance.module())) {
            throw new IllegalArgumentException("Instance module is not cached");
        }
        var factory = factories.get(instance.module());
        return factory.apply(instance);
    }
}
