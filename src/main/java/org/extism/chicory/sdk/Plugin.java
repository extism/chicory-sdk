package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

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
        private ExtismHostFunction[] hostFunctions = new ExtismHostFunction[0];
        private Logger logger;

        private Builder(Manifest manifest) {
            this.manifest = manifest;
        }

        public Builder withHostFunctions(ExtismHostFunction... hostFunctions) {
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

    private final Instance mainInstance;
    private final HostEnv hostEnv;

    Plugin(Instance main, HostEnv hostEnv) {
        this.mainInstance = main;
        this.hostEnv = hostEnv;
        mainInstance.initialize(true);
    }

    public HostEnv.Log log() {
        return hostEnv.log();
    }

    public HostEnv.Memory memory() {
        return hostEnv.memory();
    }

    public byte[] call(String funcName, byte[] input) {
        var func = mainInstance.export(funcName);
        hostEnv.setInput(input);
        var result = func.apply()[0];
        if (result == 0) {
            return hostEnv.getOutput();
        } else {
            throw new ExtismException("Failed");
        }
    }
}
