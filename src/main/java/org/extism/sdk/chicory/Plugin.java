package org.extism.sdk.chicory;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

/**
 * A Plugin instance.
 * <p>
 * Plugins can be instantiated using a {@link Plugin.Builder}, returned
 * by {@link Plugin#ofManifest(Manifest)}. The Builder allows to set options
 * on the Plugin, such as {@link HostFunction}s and the {@link Logger}.
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

        public CompiledPlugin compile() {
            var logger = this.logger == null ? new SystemLogger() : this.logger;
            Linker linker = new Linker(this.manifest, this.hostFunctions, logger);
            return linker.compile();
        }

        // Synonym for Plugin.ofManifest(Manifest).compile().instantiate();
        public Plugin build() {
            return compile().instantiate();
        }
    }

    private final Instance mainInstance;
    private final HostEnv hostEnv;

    Plugin(Instance main, HostEnv hostEnv) {
        this.mainInstance = main;
        this.hostEnv = hostEnv;
//        mainInstance.initialize(true);
    }

    public HostEnv.Log log() {
        return hostEnv.log();
    }

    public HostEnv.Memory memory() {
        return hostEnv.memory();
    }

    void setInput(byte[] input) {
        hostEnv.setInput(input);
    }

    byte[] getOutput() {
        return hostEnv.getOutput();
    }

    String getError() {
        return hostEnv.getError();
    }


    public byte[] call(String funcName, byte[] input) {
        var func = mainInstance.export(funcName);
        setInput(input);
        var results = func.apply();
        if (results == null) {
            throw new ExtismFunctionException(funcName, "The function expects an i32 return code. 0 is success and 1 is a failure.");
        }
        var result = results[0];
        if (result == 0) {
            return getOutput();
        } else {
            String error = getError();
            throw new ExtismFunctionException(funcName, error);
        }
    }
}
