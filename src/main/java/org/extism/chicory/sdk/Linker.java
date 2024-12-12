package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


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

    // Note: this is really doing nothing besides wrapping
    // the linker, because, at this time, we do not really have a concept
    // of CompiledPlugin in Chicory.
    CompiledPlugin compile() {
        return new CompiledPlugin(this);
    }

    Plugin link() {

        var dg = new DependencyGraph(logger);
        dg.setOptions(manifest.options);

        // Register the HostEnv exports.
        Map<String, String> config =
                manifest.options != null ? manifest.options.config : Map.of();
        var hostEnv = new HostEnv(new Kernel(), config, logger);
        dg.registerFunctions(hostEnv.toHostFunctions());

        // Register the WASI host functions.
        dg.registerFunctions(new WasiPreview1(
                logger,
                manifest.options == null ?
                        Manifest.Options.defaultWasiOptions() :
                        manifest.options.wasiOptions).toHostFunctions());

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

