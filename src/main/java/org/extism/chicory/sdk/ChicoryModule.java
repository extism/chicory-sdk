package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.runtime.Module;

public class ChicoryModule {

    public static Module.Builder builderFrom(ManifestWasm m, Manifest.Options opts) {
        Module.Builder mb = fromWasm(m);
        return withOptions(mb, opts);
    }

    private static Module.Builder fromWasm(ManifestWasm m) {
        if (m instanceof ManifestWasmBytes) {
            ManifestWasmBytes mwb = (ManifestWasmBytes) m;
            return Module.builder(mwb.bytes);
        } else if (m instanceof ManifestWasmPath) {
            ManifestWasmPath mwp = (ManifestWasmPath) m;
            return Module.builder(mwp.path);
        } else if (m instanceof ManifestWasmFile) {
            ManifestWasmFile mwf = (ManifestWasmFile) m;
            return Module.builder(mwf.filePath);
        } else if (m instanceof ManifestWasmUrl) {
            ManifestWasmUrl mwu = (ManifestWasmUrl) m;
            return Module.builder(mwu.getUrlAsStream());
        } else {
            throw new IllegalArgumentException("Unknown ManifestWasm type " + m.getClass());
        }
    }

    private static Module.Builder withOptions(Module.Builder mb, Manifest.Options opts) {
        if (opts == null) {
            return mb;
        }
        if (opts.aot) {
            mb.withMachineFactory(AotMachine::new);
        }
        if (!opts.validationFlags.isEmpty()) {
            throw new UnsupportedOperationException("Validation flags are not supported yet");
        }
        return mb;
    }
}
