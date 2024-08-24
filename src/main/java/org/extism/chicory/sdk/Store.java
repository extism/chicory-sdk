package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.types.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.groupingBy;
import static org.extism.chicory.sdk.ChicoryModule.bind;

// TODO the store should also store the Modules.
public class Store {
    static Logger logger = Logger.getAnonymousLogger();
    //    private final Map<String, Namespace<Export>> exportedFunctions = new HashMap<>();
    private final Map<String, TModule> modules = new HashMap<>();
    private final Map<String, TInstance> instances = new HashMap<>();
    private final Map<String, List<FunctionSignature>> importedFunctions = new HashMap<>();
    private final Map<String, List<HostFunction>> resolvedImports = new HashMap<>();


    public Store register(String name, Module module) {
        // Exports are converted into a signature bundle.
        this.modules.put(name, new TModule.ChicoryModule(name, module));

        var importSection = module.importSection();
        for (int i = 0; i < importSection.importCount(); i++) {
            Import ii = importSection.getImport(i);
            if (ii.importType() == ExternalType.FUNCTION) {
                FunctionImport fi = (FunctionImport) ii;
                var tidx = fi.typeIndex();
                FunctionType type = module.typeSection().getType(tidx);
                FunctionSignature fsig = new FunctionSignature(fi.moduleName(), fi.name(), type.params(), type.returns());
                this.importedFunctions.computeIfAbsent(name, k -> new ArrayList<>()).add(fsig);
            } // else ignore for now
        }
        return this;
    }


    public void register(HostModule hostModule) {
        // HostModules to not declare imports.
        this.modules.put(hostModule.name(), new TModule.ChicoryHostModule(hostModule));
    }

    public void resolve() {
        for (var module : importedFunctions.entrySet()) {
            // Name of the module importing the symbol.
            String moduleName = module.getKey();
            var resolvedImports = new ArrayList<HostFunction>();
            this.resolvedImports.put(moduleName, resolvedImports);
            var imports = module.getValue();
            for (var ii : imports) {
                // Name of the module whose symbol is being imported.
                String importedModuleName = ii.moduleName();
                String symbolName = ii.name();
                TModule tModule = modules.get(importedModuleName);
                if (tModule.lookup(symbolName) != null) {
                    logger.log(Level.INFO, String.format("Found EXPORTED symbol %s.%s for module %s", importedModuleName, symbolName, moduleName));
                    continue;
                }
                logger.log(Level.WARNING, String.format("NOT FOUND: symbol %s.%s for module %s", importedModuleName, symbolName, moduleName));
            }
        }
    }

    public Instance instantiate(String name) {
        return instantiate(modules.get(name)).asInstance();
    }

    public Instance instantiate(String name, Module m) {
        return instantiate(new TModule.ChicoryModule(name, m)).asInstance();
    }

    public TInstance instantiate(TModule m) {
        var importedSigs = this.importedFunctions.get(m.name());
        TInstance tInstance = instances.get(m.name());
        if (tInstance != null) {
            return tInstance;
        }

        // If all imports are satisfied, then we can instantiate.
        if (importedSigs == null) {
            if (m.isModule()) {
                // No imports: trivially satisfied.
                var instance = new TInstance.ChicoryModule(
                        m.name(), Instance.builder(m.asModule()).build().initialize(true));
                this.instances.put(m.name(), instance);
                return instance;
            } else {
                throw new UnsupportedOperationException("Cannot instantiate host modules yet");
            }
        } else {
            var sigsByModule = importedSigs.stream().collect(groupingBy(FunctionSignature::moduleName));
            for (String mname : sigsByModule.keySet()) {
                TModule tModule = this.modules.get(mname);
                instantiate(tModule);
            }
            // All imports have been now satisfied (or they failed), we can now instantiate.
            List<HostFunction> satisfiedImports = new ArrayList<>();
            for (String mname : sigsByModule.keySet()) {
                var instance = this.instances.get(mname);
                for (var sig : sigsByModule.get(mname)) {
                    HostFunction f = bind(sig, ChicoryModule.asHandle(instance.asInstance().export(sig.name())));
                    satisfiedImports.add(f);
                }
            }

            Instance instance = Instance.builder(m.asModule())
                    .withHostImports(new HostImports(satisfiedImports.toArray(new HostFunction[0]))).build()
                    .initialize(true);
            TInstance.ChicoryModule tinstance = new TInstance.ChicoryModule(m.name(), instance);
            this.instances.put(m.name(), tinstance);
            return tinstance;
        }

    }

}
