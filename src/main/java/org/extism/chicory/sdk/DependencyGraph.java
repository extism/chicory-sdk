package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.WasmFunctionHandle;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.ExportSection;
import com.dylibso.chicory.wasm.types.ExternalType;
import com.dylibso.chicory.wasm.types.FunctionImport;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.Import;
import com.dylibso.chicory.wasm.types.ImportSection;
import com.dylibso.chicory.wasm.types.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import static java.util.stream.Collectors.groupingBy;

class DependencyGraph {
    public static final String MAIN_MODULE_NAME = "main";

    private final Logger logger;

    private final Map<String, Set<String>> registeredSymbols = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();
    private final Set<String> hostModules = new HashSet<>();
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
     * <p>
     * Try to find the main module:
     * - There is always one main module
     * - If a Wasm value has the Name field set to "main" then use that module
     * - If there is only one module in the manifest then that is the main module by default
     * - Otherwise the last module listed is the main module
     */
    public void registerModules(ManifestWasm... wasms) {
        for (int i = 0; i < wasms.length; i++) {
            ManifestWasm wasm = wasms[i];
            boolean isLast = i == wasms.length - 1;
            String moduleName = wasm.name;
            var mb = ChicoryModule.fromWasm(wasm);

            if ((moduleName == null || moduleName.isEmpty() || isLast)
                    && !this.modules.containsKey(MAIN_MODULE_NAME)) {
                moduleName = MAIN_MODULE_NAME;
            }

            // TODO: checkHash(moduleName, wasm);
            registerModule(moduleName, mb);

        }
    }

    private void checkCollision(String moduleName, String symbol) {
        if (symbol == null && this.registeredSymbols.containsKey(moduleName)) {
            throw new ExtismException("Collision detected: a module with the given name already exists: " + moduleName);
        } else if (this.registeredSymbols.containsKey(moduleName) && this.registeredSymbols.get(moduleName).contains(symbol)) {
            throw new ExtismException("Collision detected: a symbol with the given name already exists: " + moduleName + "." + symbol);
        }
    }

    /**
     * Register a Module with the given name.
     */
    public void registerModule(String name, Module m) {
        checkCollision(name, null);

        ExportSection exportSection = m.wasmModule().exportSection();
        for (int i = 0; i < exportSection.exportCount(); i++) {
            Export export = exportSection.getExport(i);
            String exportName = export.name();
            this.registerSymbol(name, exportName);
        }
        modules.put(name, m);
    }

    public void registerSymbol(String name, String symbol) {
        checkCollision(name, symbol);
        registeredSymbols.computeIfAbsent(name, k -> new HashSet<>()).add(symbol);
    }

    public boolean validate() {
        boolean valid = true;
        for (var kv : modules.entrySet()) {
            Module m = kv.getValue();

            ImportSection imports = m.wasmModule().importSection();
            for (int i = 0; i < imports.importCount(); i++) {
                Import imp = imports.getImport(i);
                String moduleName = imp.moduleName();
                String symbolName = imp.name();
                if (!registeredSymbols.containsKey(moduleName) || !registeredSymbols.get(moduleName).contains(symbolName)) {
                    logger.warnf("Cannot find symbol: %s.%s\n", moduleName, symbolName);
                    valid = false;
                }
                if (!modules.containsKey(moduleName) && !hostModules.contains(moduleName)) {
                    logger.warnf("Cannot find definition for the given symbol: %s.%s\n", moduleName, symbolName);
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Instantiate is a breadth-first visit of the dependency graph, starting
     * from the `main` module, and recursively instantiating the required dependencies.
     * <p>
     * The method is idempotent, invoking it twice causes it to return the same instance.
     *
     * @return an instance of the main module.
     */
    public Instance instantiate() {
        Instance mainInstance = this.getMainInstance();
        if (mainInstance != null) {
            return mainInstance;
        }

        if (!validate()) {
            throw new ExtismException("Unresolved symbols");
        }

        Stack<String> unresolved = new Stack<>();
        unresolved.push(MAIN_MODULE_NAME);

        while (!unresolved.isEmpty()) {
            String moduleId = unresolved.peek();
            Module m = this.modules.get(moduleId);
            boolean satisfied = true;
            List<HostFunction> trampolines = new ArrayList<>();
            ImportSection imports = m.wasmModule().importSection();
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
                                trampolines.add(registerTrampoline((FunctionImport) mi, m));
                            } else {
                                throw new ExtismException("cycle detected on a non-function");
                            }
                        }
                    } else if (!this.instances.containsKey(requiredModule) && !this.hostModules.contains(requiredModule)) {
                        // No such instance nor registered host function; we schedule this module for visiting.
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
                instantiate(moduleId, trampolines);
            }
        }

        // We are now ready to resolve all the trampolines.
        for (var t : trampolines.entrySet()) {
            QualifiedName name = t.getKey();
            Trampoline trampoline = t.getValue();

            ExportFunction ef = instances.get(name.moduleName).export(name.fieldName);
            trampoline.resolveFunction(ef);
        }

        // We can now initialize all modules.
        for (var inst : this.instances.values()) {
            inst.initialize(true);
        }

        return this.getMainInstance();
    }

    private Instance instantiate(String moduleId, List<HostFunction> moreHostFunctions) {
        Module m = this.modules.get(moduleId);
        Objects.requireNonNull(m);

        HostImports extendedHostImports =
                mergeHostImports(store.toHostImports(), moreHostFunctions);

        Instance instance =
                ChicoryModule.instanceWithOptions(
                        Module.builder(m.wasmModule()), this.options)
                        .withHostImports(extendedHostImports)
                        .withStart(false)
                        .build().instantiate();
        this.store.register(moduleId, instance);
        this.instances.put(moduleId, instance);
        return instance;
    }

    private HostImports mergeHostImports(HostImports hostImports, List<HostFunction> trampolines) {
        HostFunction[] hostFunctions = hostImports.functions();
        List<HostFunction> mergedList = new ArrayList<>(trampolines);
        for (HostFunction fn : hostFunctions) {
            for (HostFunction t : trampolines) {
                if (t.moduleName().equals(fn.fieldName()) && t.fieldName().equals(fn.fieldName())) {
                    // If one such case exists, the "proper" function takes precedence over the trampoline.
                    mergedList.remove(t);
                }
            }
            mergedList.add(fn);
        }
        return new HostImports(
                mergedList.toArray(new HostFunction[mergedList.size()]),
                hostImports.globals(),
                hostImports.memories(),
                hostImports.tables());
    }

    private HostFunction registerTrampoline(FunctionImport f, Module m) {
        // Trampolines are singletons for each <moduleName, name> pair.
        // Trampolines are not registered into the store, as they are not "real" functions.
        // They are instead kept separately and passed explicitly to the instance.
        Trampoline trampoline = this.trampolines.computeIfAbsent(
                new QualifiedName(f.moduleName(), f.name()), k -> new Trampoline());
        var functionType = m.wasmModule().typeSection().getType(f.typeIndex());
        return trampoline.asHostFunction(f.moduleName(), f.name(), functionType);
    }

    /**
     * Register the given host functions in the store. Each host function
     * has a "module name" and a symbol name, thus we register each module name
     * in the "hostModules" set.
     */
    public void registerFunctions(HostFunction... functions) {
        store.addFunction(functions);
        for (HostFunction f : functions) {
            this.hostModules.add(f.moduleName());
            registerSymbol(f.moduleName(), f.fieldName());
        }
    }

    /**
     * @return a named instance with the given name. The method is idempotent,
     * invoking it twice causes it to return the same instance.
     */
    public Instance getInstance(String moduleName) {
        if (instances.containsKey(moduleName)) {
            return instances.get(moduleName);
        } else {
            return instantiate(moduleName, List.of());
        }
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

    /**
     * A pair moduleName, symbol name.
     */
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