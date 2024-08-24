package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.ExternalType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChicoryModule {

    public static Instance.Builder builderFrom(ManifestWasm mw, Manifest.Options opts) {
        Module m = fromWasm(mw);
        return withOptions(m, opts);
    }

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

    public static FunctionSignatureBundle toSignatureBundle(String name, Module module) {
        List<FunctionSignature> signatures = new ArrayList<>();
        var exportSection = module.exportSection();
        for (int i = 0; i < exportSection.exportCount(); i++) {
            var export = exportSection.getExport(i);
            if (export.exportType() == ExternalType.FUNCTION) {
                var type = module.functionSection().getFunctionType(export.index(), module.typeSection());
                signatures.add(new FunctionSignature(name, export.name(), type.params(), type.returns()));
            }
        }
        return new FunctionSignatureBundle(name, signatures.toArray(new FunctionSignature[signatures.size()]));
    }

    public static HostFunction bind(FunctionSignature fsig, WasmFunctionHandle handle) {
        return new HostFunction(handle, fsig.moduleName(), fsig.name(), fsig.paramTypes(), fsig.returnTypes());
    }

    public static WasmFunctionHandle asHandle(ExportFunction ef) {
        return (inst, args) -> ef.apply(args);
    }
    private static Instance.Builder withOptions(Module m, Manifest.Options opts) {
        Instance.Builder builder = Instance.builder(m);
        if (opts == null) {
            return builder;
        }
        if (opts.aot) {
            builder.withMachineFactory(AotMachine::new);
        }
        if (!opts.validationFlags.isEmpty()) {
            throw new UnsupportedOperationException("Validation flags are not supported yet");
        }
        return builder;
    }
}
