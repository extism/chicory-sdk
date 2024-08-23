package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.FunctionSignature;
import com.dylibso.chicory.runtime.FunctionSignatureBundle;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostModule;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.ExternalType;
import com.dylibso.chicory.wasm.types.Import;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO the store should also store the Modules.
public class Store {
    private final Map<String, Namespace<Export>> exportedFunctions = new HashMap<>();
    private final Map<String, FunctionSignatureBundle> functionSignatures = new HashMap<>();
    private final Map<String, List<Import>> imports = new HashMap<>();
    private final Map<String, List<HostFunction>> resolvedImports = new HashMap<>();


    public Store register(String name, Module module) {
        var signatureBundle = ChicoryModule.toSignatureBundle(name, module);
        this.register(signatureBundle);

        var importSection = module.importSection();
        for (int i = 0; i < importSection.importCount(); i++) {
            Import ii = importSection.getImport(i);
            if (ii.importType() == ExternalType.FUNCTION) {
                this.imports.computeIfAbsent(
                        name, k -> new ArrayList<>())
                        .add(ii);
            } else {
                // ignore for now
            }
        }
        return this;
    }


    public void register(HostModule hostModule) {
        this.functionSignatures.put(hostModule.name(), (FunctionSignatureBundle) hostModule); // FIXME
    }

    public void resolve() {
        for (var module : imports.entrySet()) {
            // Name of the module importing the symbol.
            String moduleName = module.getKey();
            var resolvedImports = new ArrayList<HostFunction>();
            this.resolvedImports.put(moduleName, resolvedImports);
            List<Import> imports = module.getValue();
            for (Import ii : imports) {
                // Name of the module whose symbol is being imported.
                String importedModuleName = ii.moduleName();
                String symbolName = ii.name();
                boolean found = lookupSymbol(exportedFunctions, importedModuleName, symbolName);
                if (found) {
                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Found HOST symbol %s.%s for module %s", importedModuleName, symbolName, moduleName));
                    continue;
                }

                found = lookupSymbolInBundle(functionSignatures, importedModuleName, symbolName);
                if (found) {
                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Found EXPORTED symbol %s.%s for module %s", importedModuleName, symbolName, moduleName));
                    continue;
                }

                Logger.getAnonymousLogger().log(Level.WARNING, String.format("NOT FOUND: symbol %s.%s for module %s", importedModuleName, symbolName, moduleName));
            }
        }
    }

    private <T> boolean lookupSymbol(Map<String, Namespace<T>> nss, String importedModuleName, String symbolName) {
        if (nss.containsKey(importedModuleName)) {
            var ns = nss.get(importedModuleName);
            return ns.lookup(symbolName).isPresent();
        }
        return false;
    }

    private <T> boolean lookupSymbolInBundle(Map<String, FunctionSignatureBundle> nss, String importedModuleName, String symbolName) {
        if (nss.containsKey(importedModuleName)) {
            var ns = nss.get(importedModuleName);
            for (FunctionSignature signature : ns.signatures()) {
                if (signature.name().equals(symbolName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}


class Namespace<T> {
    private final String name;
    private final Map<String, T> items;

    Namespace(String name) {
        this.name = name;
        this.items = new HashMap<>();
    }

    public Namespace<T> register(String itemId, T value) {
        items.put(itemId, value);
        return this;
    }

    public Optional<T> lookup(String itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

}