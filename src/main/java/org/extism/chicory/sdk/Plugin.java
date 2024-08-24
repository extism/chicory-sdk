package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;

import static com.dylibso.chicory.wasm.types.Value.i64;

/**
 * A Plugin instance.
 *
 * Plugins can be instantiated using a {@link Plugin.Builder}, returned
 * by {@link Plugin#ofManifest(Manifest)}. The Builder allows to set options
 * on the Plugin, such as {@link HostFunction}s and the {@link Logger}.
 *
 */
public class Plugin {

    public static Builder ofManifest(Manifest manifest) {
        return new Builder(manifest);
    }

    public static class Builder {

        private final Manifest manifest;
        private HostFunction[] hostFunctions = new HostFunction[0];
        private Logger logger;

        private Builder(Manifest manifest) {
            this.manifest = manifest;
        }

        public Builder withHostFunctions(HostFunction... hostFunctions) {
            this.hostFunctions = hostFunctions;
            return this;
        }

        public Builder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Plugin build() {
            var logger = this.logger == null ? new SystemLogger() : this.logger;
            Linker linker = new Linker(this.manifest, this.hostFunctions, logger);
            return linker.link();
        }
    }

    private final Kernel kernel;

    private final Instance mainInstance;

    Plugin(Instance main, Kernel kernel) {
        this.kernel = kernel;
        this.mainInstance = main;
        mainInstance.initialize(true);
    }

    public byte[] call(String funcName, byte[] input) {
        var func = mainInstance.export(funcName);
        kernel.setInput(input);
        var result = func.apply()[0].asInt();
        if (result == 0) {
            return kernel.getOutput();
        } else {
            throw new ExtismException("Failed");
        }
    }
}
