package org.extism.sdk.chicory.core;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;

import java.util.Arrays;


/**
 * Links together the modules in the given manifest with the given host functions
 * and predefined support modules (e.g. the {@link Kernel}.
 * <p>
 * Returns a {@link Plugin}.
 */
class Linker {
    private final Manifest manifest;
    private final ExtismHostFunction[] hostFunctions;
    private final Logger logger;

    Linker(Manifest manifest, ExtismHostFunction[] hostFunctions, Logger logger) {
        this.manifest = manifest;
        this.hostFunctions = hostFunctions;
        this.logger = logger;
    }

    CompiledPlugin compile() {
        return new CompiledPlugin(this);
    }

    Plugin link() {
        var dg = new DependencyGraph(logger);

        ConfigProvider config;
        String[] allowedHosts;
        WasiOptions wasiOptions;
        CachedAotMachineFactory aotMachineFactory;
        HttpConfig httpConfig;
        dg.setOptions(manifest.options);
        config = manifest.options.config;
        allowedHosts = manifest.options.allowedHosts;
        wasiOptions = manifest.options.wasiOptions;
        aotMachineFactory = manifest.options.aot? new CachedAotMachineFactory() : null;
        httpConfig = manifest.options.httpConfig;

        // Register the HostEnv exports.
        var hostEnv = new HostEnv(new Kernel(aotMachineFactory), config, allowedHosts, httpConfig, logger);
        dg.registerFunctions(hostEnv.toHostFunctions());

        // Register the WASI host functions.
        if (wasiOptions != null) {
            dg.registerFunctions(
                     WasiPreview1.builder()
                             .withLogger(logger)
                             .withOptions(wasiOptions)
                             .build()
                             .toHostFunctions());
        }

        // Register the user-provided host functions.
        dg.registerFunctions(Arrays.stream(this.hostFunctions)
                .map(ExtismHostFunction::asHostFunction)
                .toArray(HostFunction[]::new));

        // Register all the modules declared in the manifest.
        dg.registerModules(manifest.wasms);

        // Instantiate the main module, and, recursively, all of its dependencies.
        Instance main = dg.instantiate();

        Plugin p = new Plugin(main, hostEnv);
        CurrentPlugin curr = new CurrentPlugin(p);

        // Bind all host functions to a CurrentPlugin wrapper for this Plugin.
        for (ExtismHostFunction fn : this.hostFunctions) {
            fn.bind(curr);
        }

        return p;
    }

}

