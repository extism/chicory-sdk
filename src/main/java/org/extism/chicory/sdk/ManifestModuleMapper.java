package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.runtime.Module;

class ManifestModuleMapper {
    private final Manifest manifest;

    ManifestModuleMapper(Manifest manifest) {
        this.manifest = manifest;
    }

    Module.Builder toModuleBuilder() {
        if (manifest.wasms.length > 1) {
            throw new UnsupportedOperationException(
                    "Manifests of multiple wasm files are not supported yet!");
        }
        Module.Builder mb = wasmToModuleBuilder(manifest.wasms[0]);
        return withOptions(mb, manifest.options);
    }

    private Module.Builder wasmToModuleBuilder(ManifestWasm m) {
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

    private Module.Builder withOptions(Module.Builder mb, Manifest.Options opts) {
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
