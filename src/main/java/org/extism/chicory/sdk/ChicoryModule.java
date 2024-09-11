package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.Parser;

import java.nio.file.Path;

public class ChicoryModule {

    static final boolean IS_NATIVE_IMAGE_AOT = Boolean.getBoolean("com.oracle.graalvm.isaot");

    public static Module fromWasm(ManifestWasm m) {
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

    public static Instance.Builder instanceWithOptions(Module m, Manifest.Options opts) {
        Instance.Builder builder = Instance.builder(m);
        if (opts == null) {
            return builder;
        }
        // This feature is not compatibly with the native-image builder.
        if (opts.aot && !IS_NATIVE_IMAGE_AOT) {
            builder.withMachineFactory(AotMachine::new);
        }
        if (!opts.validationFlags.isEmpty()) {
            throw new UnsupportedOperationException("Validation flags are not supported yet");
        }
        return builder;
    }
}
