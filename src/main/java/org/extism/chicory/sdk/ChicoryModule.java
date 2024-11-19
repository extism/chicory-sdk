package org.extism.chicory.sdk;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.Parser;

import java.nio.file.Path;

class ChicoryModule {

    static final boolean IS_NATIVE_IMAGE_AOT = Boolean.getBoolean("com.oracle.graalvm.isaot");

    static Module fromWasm(ManifestWasm m) {
        if (m instanceof ManifestWasmBytes) {
            ManifestWasmBytes mwb = (ManifestWasmBytes) m;
            return Parser.parse(mwb.bytes);
        } else if (m instanceof ManifestWasmPath) {
            ManifestWasmPath mwp = (ManifestWasmPath) m;
            return Parser.parse(Path.of(mwp.path));
        } else if (m instanceof ManifestWasmFile) {
            ManifestWasmFile mwf = (ManifestWasmFile) m;
            return Parser.parse(mwf.filePath);
        } else if (m instanceof ManifestWasmUrl) {
            ManifestWasmUrl mwu = (ManifestWasmUrl) m;
            return Parser.parse(mwu.getUrlAsStream());
        } else {
            throw new IllegalArgumentException("Unknown ManifestWasm type " + m.getClass());
        }
    }

    static Instance.Builder instanceWithOptions(Instance.Builder m, Manifest.Options opts) {
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
