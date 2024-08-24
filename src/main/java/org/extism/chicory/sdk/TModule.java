package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.FunctionSignature;
import com.dylibso.chicory.runtime.FunctionSignatureBundle;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.runtime.HostModule;

import java.util.Arrays;

public interface TModule {
    default boolean isModule() { return this instanceof TModule.ChicoryModule; }
    default Module asModule() { return ((ChicoryModule) this).module; }
    default boolean isHostModule() { return this instanceof TModule.ChicoryHostModule; }
    default HostModule asHostModule() { return ((ChicoryHostModule) this).module; }

    FunctionSignature lookup(String symbolName);

    String name();

    class ChicoryModule implements TModule {
        private final String name;
        public final Module module;
        public final FunctionSignatureBundle signatures;
        public ChicoryModule(String name, Module module) {
            this.name = name;
            this.module = module;
            this.signatures = org.extism.chicory.sdk.ChicoryModule.toSignatureBundle(name, module);
        }
        @Override
        public String name() {return name;}
        @Override
        public FunctionSignature lookup(String symbolName) {
            return Arrays.stream(signatures.signatures()).filter(s -> s.name().equals(symbolName)).findFirst().orElse(null);
        }
    }
    class ChicoryHostModule implements TModule {
        public final HostModule module;
        public ChicoryHostModule(HostModule module) {this.module = module;}
        @Override
        public FunctionSignature lookup(String symbolName) {
            return Arrays.stream(module.signatures()).filter(s -> s.name().equals(symbolName)).findFirst().orElse(null);
        }
        @Override
        public String name() {
            return module.name();
        }
    }
}
