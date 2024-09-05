package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Store;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.Module;


/**
 * Links together the modules in the given manifest with the given host functions
 * and predefined support modules (e.g. the {@link Kernel}.
 * <p>
 * Returns a {@link Plugin}.
 */
class Linker {
    public static final String EXTISM_NS = "extism:host/env";
    private final Manifest manifest;
    private final HostFunction[] hostFunctions;
    private final Logger logger;

    Linker(Manifest manifest, HostFunction[] hostFunctions, Logger logger) {
        this.manifest = manifest;
        this.hostFunctions = hostFunctions;
        this.logger = logger;
    }

    /*
     * Try to find the main module:
     *  - There is always one main module
     *  - If a Wasm value has the Name field set to "main" then use that module
     *  - If there is only one module in the manifest then that is the main module by default
     *  - Otherwise the last module listed is the main module
     *
     */
    public Plugin link() {
        var wasip1 = new WasiPreview1(logger);
        var kernel = Kernel.module();

        Store store = new Store();

        store.addFunction(wasip1.toHostFunctions());
        Instance kernelInstance =
                store.instantiate(Kernel.IMPORT_MODULE_NAME, kernel);

        ManifestWasm[] wasms = this.manifest.wasms;
        Instance mainModule = null;

        sortWasms(wasms);

        for (int i = 0; i < wasms.length; i++) {
            boolean isMain = false;
            ManifestWasm wasm = wasms[i];
            boolean isLast = i == wasms.length - 1;
            String moduleName = wasm.name;
            Module m = ChicoryModule.fromWasm(wasm);

            if ((moduleName == null || moduleName.isEmpty() || isLast) && mainModule == null) {
                moduleName = "main";
                isMain = true;
            }

            checkCollision(moduleName, wasms);
            checkHash(moduleName, wasm);

            Instance instance = store.instantiate(moduleName, m);
            if (isMain) {
                mainModule = instance;
            }
        }

        return new Plugin(mainModule, new Kernel(kernelInstance));

    }

    private void sortWasms(ManifestWasm[] wasms) {
        Store store = new Store();

    }

    /**
     * @throws ExtismException on name collision.
     */
    private void checkCollision(String moduleName, ManifestWasm[] wasms) {
        // FIXME: check both host imports and modules.
        if (moduleName.equals(EXTISM_NS)) {
            throw new ExtismException(String.format("Module name collision: %s", moduleName));
        }

        // FIXME: check collision on already processed modules
    }

    private void checkHash(String moduleName, ManifestWasm wasm) {
        // FIXME: add hash check.
    }


}

