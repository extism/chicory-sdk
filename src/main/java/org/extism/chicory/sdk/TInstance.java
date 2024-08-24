package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.wasm.Module;

import java.util.Arrays;

import static org.extism.chicory.sdk.ChicoryModule.*;

public interface TInstance {
    default boolean isModule() { return this instanceof TInstance.ChicoryModule; }
    default Instance asInstance() { return ((ChicoryModule) this).instance; }
    default boolean isHostModule() { return this instanceof TInstance.ChicoryHostModule; }
    default HostModuleInstance asHostModuleInstance() { return ((ChicoryHostModule) this).instance; }

    HostFunction lookup(String symbolName);

    class ChicoryModule implements TInstance {
        public final Instance instance;
        public final FunctionSignatureBundle bundle;
        public ChicoryModule(String name, Instance instance) {
            this.instance = instance;
            this.bundle = toSignatureBundle(name, instance.module());
        }
        public HostFunction lookup(String symbolName) {
            ExportFunction export = instance.export(symbolName);
            FunctionSignature sig = Arrays.stream(bundle.signatures())
                    .filter(s -> s.name().equals(symbolName)).findFirst().orElse(null);
            if (sig == null) {
                return null;
            }
            return bind(sig, asHandle(export));
        }
    }
    class ChicoryHostModule implements TInstance {
        public final HostModuleInstance instance;
        public ChicoryHostModule(HostModuleInstance instance) {this.instance = instance;}
        @Override
        public HostFunction lookup(String symbolName) {
                return Arrays.stream(instance.hostFunctions())
                    .filter(s -> s.fieldName().equals(symbolName)).findFirst().orElse(null);
        }
    }
}
