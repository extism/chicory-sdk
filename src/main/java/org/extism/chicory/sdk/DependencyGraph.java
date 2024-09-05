package org.extism.chicory.sdk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Store;
import com.dylibso.chicory.runtime.WasmFunctionHandle;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.ExportSection;
import com.dylibso.chicory.wasm.types.Import;
import com.dylibso.chicory.wasm.types.ImportSection;
import com.dylibso.chicory.wasm.types.Value;

public class DependencyGraph {
    private String mainId;

    Map<String, Set<String>> edges = new HashMap<>();
    Map<String, Module> modules = new HashMap<>();
    Map<String, Instance> instances = new HashMap<>();
    Map<String, Trampoline> trampolines = new HashMap<>();

    Store store = new Store();

    public void setMain(String mainId) {
        this.mainId = mainId;
    }

    public void registerSymbol(String name, String symbol) {
        edges.computeIfAbsent(name, k -> new HashSet<>()).add(symbol);
    }

    public void register(String name, Module m) {
        ExportSection exportSection = m.exportSection();
        for (int i = 0; i < exportSection.exportCount(); i++) {
            Export export = exportSection.getExport(i);
            String exportName = export.name();
            this.registerSymbol(name, exportName);
        }
        modules.put(name, m);
    }

    public boolean validate() {
        boolean valid = true;
        for (var kv : modules.entrySet()) {
            String name = kv.getKey();
            Module m = kv.getValue();

            ImportSection imports = m.importSection();
            for (int i = 0; i < imports.importCount(); i++) {
                Import imp = imports.getImport(i);
                String moduleName = imp.moduleName();
                if (!edges.containsKey(moduleName) || !edges.get(moduleName).contains(imp.name())) {
                    System.err.printf("Cannot find symbol: %s.%s\n", moduleName, name);
                }
            }
        }
        return valid;
    }

    public void resolve() {
        if (!validate()) {
            throw new ExtismException("Unresolved symbols");
        }

        String moduleId = mainId;
        while (true) {
            Module m = modules.get(mainId);
            Instance.builder(m).withHostImports()
        }
    }

}

class Trampoline implements WasmFunctionHandle {
    WasmFunctionHandle f =
            (Instance instance, Value... args) -> {
                throw new ExtismException("Unresolved trampoline");
            };

    public void resolveFunction(HostFunction hf) {
        this.f = hf.handle();
    }

    @Override
    public Value[] apply(Instance instance, Value... args) {
        return f.apply(instance, args);
    }
}