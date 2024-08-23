package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostModule;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.Module;


/**
 * Links together the modules in the given manifest with the given host functions
 * and predefined support modules (e.g. the {@link Kernel}.
 * <p>
 * Returns a {@link LinkedModules} instance, which in turn can be converted
 * into a {@link Plugin}.
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
    public LinkedModules link() {
        var wasip1 = wasip1();
        Kernel kernel = kernel();

        Store store = new Store();

        store.register(wasip1);
        store.register(Kernel.IMPORT_MODULE_NAME, kernel.module());


        ManifestWasm[] wasms = this.manifest.wasms;
        int mainModule = -1;

        for (int i = 0; i < wasms.length; i++) {
            ManifestWasm wasm = wasms[i];
            boolean isLast = i == wasms.length - 1;
            String moduleName = wasm.name;
            Module m = ChicoryModule.fromWasm(wasm);

            if ((moduleName == null || moduleName.isEmpty() || isLast) && mainModule < 0) {
                moduleName = "main";
                mainModule = i;
            }

            checkCollision(moduleName, wasms);
            checkHash(moduleName, wasm);

            store.register(moduleName, m);
        }

        store.resolve();

        return new LinkedModules(kernel, store, mainModule);
    }

    private HostModule wasip1() {
        return WasiPreview1.toHostModule();
    }

    private Kernel kernel() {
        var kernel = new Kernel(logger, manifest.options);
        return kernel;
    }

    private static HostFunction[] concat(
            HostFunction[] kernelFuncs, HostFunction[] hostFunctions, HostFunction[] wasiHostFunctions) {
        // concat list of host functions
        var hostFuncList = new HostFunction[hostFunctions.length + kernelFuncs.length + wasiHostFunctions.length];
        System.arraycopy(kernelFuncs, 0, hostFuncList, 0, kernelFuncs.length);
        System.arraycopy(hostFunctions, 0, hostFuncList, kernelFuncs.length, hostFunctions.length);
        System.arraycopy(wasiHostFunctions, 0, hostFuncList, kernelFuncs.length + hostFunctions.length, wasiHostFunctions.length);
        return hostFuncList;
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

/**
 * A collection of modules that have been successfully linked together.
 * LinkedModules can be converted to a Plugin using the {@link #toPlugin()}
 * method.
 */
class LinkedModules {

    private final Kernel kernel;
    private final Store store;
    private final int mainModule;

    LinkedModules(Kernel kernel, Store store, int mainModule) {
        this.kernel = kernel;
        this.store = store;
        this.mainModule = mainModule;
    }

    public Plugin toPlugin() {
        throw new UnsupportedOperationException("todo");

//        Instance[] instances = null;
//        Instance[] instances = new Instance[moduleBuilders.length];
//        Instance.Builder[] builders = this.moduleBuilders;
//        for (int i = 0; i < builders.length; i++) {
//            Instance.Builder mb = builders[i];
//            Instance m = mb.build();
//            instances[i] = m.initialize(false);
//        }
//
//        return new Plugin(kernel, instances, mainModule);
    }
}
