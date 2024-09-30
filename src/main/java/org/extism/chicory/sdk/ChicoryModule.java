package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.runtime.Module;

import java.nio.file.Path;

class ChicoryModule {

    static final boolean IS_NATIVE_IMAGE_AOT = Boolean.getBoolean("com.oracle.graalvm.isaot");

    static Module fromWasm(ManifestWasm m) {
        if (m instanceof ManifestWasmBytes) {
            ManifestWasmBytes mwb = (ManifestWasmBytes) m;
            return Module.builder(mwb.bytes).build();
        } else if (m instanceof ManifestWasmPath) {
            ManifestWasmPath mwp = (ManifestWasmPath) m;
            return Module.builder(Path.of(mwp.path)).build();
        } else if (m instanceof ManifestWasmFile) {
            ManifestWasmFile mwf = (ManifestWasmFile) m;
            return Module.builder(mwf.filePath).build();
        } else if (m instanceof ManifestWasmUrl) {
            ManifestWasmUrl mwu = (ManifestWasmUrl) m;
            return Module.builder(mwu.getUrlAsStream()).build();
        } else {
            throw new IllegalArgumentException("Unknown ManifestWasm type " + m.getClass());
        }
    }

    static Module.Builder instanceWithOptions(Module.Builder m, Manifest.Options opts) {
        if (opts == null) {
            return m;
        }
        // This feature is not compatibly with the native-image builder.
        if (opts.aot && !IS_NATIVE_IMAGE_AOT) {
            m.withMachineFactory(AotMachine::new);
        }
        if (!opts.validationFlags.isEmpty()) {
            throw new UnsupportedOperationException("Validation flags are not supported yet");
        }
        return m;
    }
}
