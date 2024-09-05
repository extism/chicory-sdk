package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Store;
import com.dylibso.chicory.runtime.WasmFunctionHandle;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.ExportSection;
import com.dylibso.chicory.wasm.types.ExternalType;
import com.dylibso.chicory.wasm.types.FunctionImport;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.Import;
import com.dylibso.chicory.wasm.types.ImportSection;
import com.dylibso.chicory.wasm.types.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import static java.util.stream.Collectors.groupingBy;

public class DependencyGraph {
    public static final String MAIN_MODULE_NAME = "main";

    private final Logger logger;

    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();
    private final Map<String, Instance> instances = new HashMap<>();
    private final Map<QualifiedName, Trampoline> trampolines = new HashMap<>();

    private final Store store = new Store();
    private Manifest.Options options;

    public DependencyGraph(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set the instantiation options.
     */
    public void setOptions(Manifest.Options options) {
        this.options = options;
    }

    /**
     * Registers all the given named modules, and tries to look for a `main`.
     *
     * Try to find the main module:
     *  - There is always one main module
     *  - If a Wasm value has the Name field set to "main" then use that module
     *  - If there is only one module in the manifest then that is the main module by default
     *  - Otherwise the last module listed is the main module
     *
     */
    public void registerModules(ManifestWasm... wasms) {
        for (int i = 0; i < wasms.length; i++) {
            ManifestWasm wasm = wasms[i];
            boolean isLast = i == wasms.length - 1;
            String moduleName = wasm.name;
            Module m = ChicoryModule.fromWasm(wasm);

            if ((moduleName == null || moduleName.isEmpty() || isLast)
                    && !this.modules.containsKey(MAIN_MODULE_NAME)) {
                moduleName = MAIN_MODULE_NAME;
            }

            // TODO: checkHash(moduleName, wasm);
            registerModule(moduleName, m);

        }
    }

    private void checkCollision(String moduleName, String symbol) {
        if (symbol == null && this.edges.containsKey(moduleName)) {
            throw new ExtismException("Collision detected: a module with the given name already exists: " + moduleName);
        } else if (this.edges.containsKey(moduleName) && this.edges.get(moduleName).contains(symbol)) {
            throw new ExtismException("Collision detected: a symbol with the given name already exists: " + moduleName + "." + symbol);
        }
    }

    public void registerModule(String name, Module m) {
        checkCollision(name, null);

        ExportSection exportSection = m.exportSection();
        for (int i = 0; i < exportSection.exportCount(); i++) {
            Export export = exportSection.getExport(i);
            String exportName = export.name();
            this.registerSymbol(name, exportName);
        }
        modules.put(name, m);
    }

    public void registerSymbol(String name, String symbol) {
        checkCollision(name, symbol);
        edges.computeIfAbsent(name, k -> new HashSet<>()).add(symbol);
    }

    public boolean validate() {
        boolean valid = true;
        for (var kv : modules.entrySet()) {
            Module m = kv.getValue();

            ImportSection imports = m.importSection();
            for (int i = 0; i < imports.importCount(); i++) {
                Import imp = imports.getImport(i);
                String moduleName = imp.moduleName();
                String symbolName = imp.name();
                if (!edges.containsKey(moduleName) || !edges.get(moduleName).contains(symbolName)) {
                    logger.debug(String.format("Cannot find symbol: %s.%s\n", moduleName, symbolName));
                }
            }
        }
        return valid;
    }

    /**
     * Instantiate is a breadth-first visit of the dependency graph, starting
     * from the `main` module, and recursively instantiating the required dependencies.
     *
     * @return an instance of the main module.
     */
    public Instance instantiate() {
        if (!validate()) {
            throw new ExtismException("Unresolved symbols");
        }

        Stack<String> unresolved = new Stack<>();
        unresolved.push(MAIN_MODULE_NAME);

        while (!unresolved.isEmpty()) {
            String moduleId = unresolved.peek();
            Module m = this.modules.get(moduleId);
            boolean satisfied = true;

            ImportSection imports = m.importSection();
            // We assume that each unique `name` in an import of the form `name.symbol`
            // is registered as a module with that name
            //
            // FIXME: this is actually a strong assumption, because we could
            // define "overrides" by overwriting individual `name.symbol` in our table.
            var requiredModules = imports.stream().collect(groupingBy(Import::moduleName));

            if (!requiredModules.isEmpty()) {
                // We need to check whether the given import is available
                for (String requiredModule : requiredModules.keySet()) {
                    if (unresolved.contains(requiredModule)) {
                        // This is a cycle!
                        var moduleImports = requiredModules.get(requiredModule);
                        for (Import mi : moduleImports) {
                            if (mi.importType() == ExternalType.FUNCTION) {
                                // It's ok, we just add one little indirection.
                                // This will be resolved at the end, when everything is settled.
                                registerTrampoline((FunctionImport) mi, m);
                            } else {
                                throw new ExtismException("cycle detected on a non-function");
                            }
                        }
                    } else if (!this.instances.containsKey(requiredModule)) {
                        // No such instance, we schedule this module for visiting.
                        satisfied = false;
                        unresolved.push(requiredModule);
                    }
                }
            }

            // The store already contains everything we need,
            // we can proceed with pop the name from the stack
            // and instantiate.
            if (satisfied) {
                unresolved.pop();
                Instance instance =
                        ChicoryModule.instanceWithOptions(m, this.options)
                                .withHostImports(store.toHostImports())
                                .build();
                this.store.register(moduleId, instance);
                this.instances.put(moduleId, instance);
            }

        }

        // We are now ready to resolve all the trampolines.
        for (var t : trampolines.entrySet()) {
            QualifiedName name = t.getKey();
            Trampoline trampoline = t.getValue();

            ExportFunction ef = instances.get(name.moduleName).export(name.fieldName);
            trampoline.resolveFunction(ef);
        }

        return this.getMainInstance();
    }

    private void registerTrampoline(FunctionImport f, Module m) {
        var trampoline = new Trampoline();
        var functionType = m.typeSection().getType(f.typeIndex());

        this.trampolines.put(new QualifiedName(f.moduleName(), f.name()), trampoline);
        this.store.addFunction(trampoline.asHostFunction(f.moduleName(), f.name(), functionType));
    }

    /**
     * Register the given host functions in the store.
     */
    public void registerFunctions(HostFunction... functions) {
        store.addFunction(functions);
        for (HostFunction f : functions) {
            registerSymbol(f.moduleName(), f.fieldName());
        }
    }

    /**
     * @return a named instance with the given name.
     */
    public Instance getInstance(String moduleName) {
        return instances.get(moduleName);
    }

    /**
     * @return the main instance.
     */
    private Instance getMainInstance() {
        return this.instances.get(MAIN_MODULE_NAME);
    }

    static final class Trampoline implements WasmFunctionHandle {
        WasmFunctionHandle f =
                (Instance instance, Value... args) -> {
                    throw new ExtismException("Unresolved trampoline");
                };

        public void resolveFunction(HostFunction hf) {
            this.f = hf.handle();
        }

        public void resolveFunction(ExportFunction ef) {
            this.f = (Instance instance, Value... args) -> ef.apply(args);
        }

        @Override
        public Value[] apply(Instance instance, Value... args) {
            return f.apply(instance, args);
        }

        public HostFunction asHostFunction(String moduleName, String name, FunctionType functionType) {
            return new HostFunction(this, moduleName, name,
                    functionType.params(), functionType.returns());
        }
    }

    static final class QualifiedName {
        final String moduleName;
        final String fieldName;

        public QualifiedName(String moduleName, String fieldName) {
            this.moduleName = moduleName;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof QualifiedName)) {
                return false;
            }
            QualifiedName qualifiedName = (QualifiedName) o;
            return Objects.equals(moduleName, qualifiedName.moduleName)
                    && Objects.equals(fieldName, qualifiedName.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleName, fieldName);
        }
    }

}