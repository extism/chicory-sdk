package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiPreview1;


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

    public Plugin link() {

        var dg = new DependencyGraph(logger);
        dg.setOptions(manifest.options);

        // Register the Kernel module, usually not present in the manifest.
        dg.registerModule(Kernel.IMPORT_MODULE_NAME, Kernel.module());

        // Register the WASI host functions.
        dg.registerFunctions(new WasiPreview1(logger).toHostFunctions());

        // Register the user-provided host functions.
        dg.registerFunctions(this.hostFunctions);

        // Register all the modules declared in the manifest.
        dg.registerModules(manifest.wasms);

        // Instantiate the main module, and, recursively, all of its dependencies.
        Instance main = dg.instantiate();
        // The kernel has been now instantiated, get a handle for it.
        Instance kernelInstance = dg.getInstance(Kernel.IMPORT_MODULE_NAME);

        return new Plugin(main, new Kernel(kernelInstance));
    }

}

