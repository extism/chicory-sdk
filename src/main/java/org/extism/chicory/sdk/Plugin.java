package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;

import java.util.Arrays;
import java.util.Map;

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
            return new Plugin(manifest, hostFunctions, logger);
        }
    }

    private final Manifest manifest;
    private final Instance instance;
    private final HostImports imports;
    private final Kernel kernel;
    private final HostEnv hostEnv;

    private Plugin(Manifest manifest) {
        this(manifest, new ExtismHostFunction[]{}, null);
    }

    private Plugin(Manifest manifest, ExtismHostFunction[] hostFunctions, Logger logger) {
        if (logger == null) {
            logger = new SystemLogger();
        }

        this.kernel = new Kernel(logger);
        this.hostEnv = new HostEnv(kernel, Map.of(), logger);
        this.manifest = manifest;

        // TODO: Expand WASI Support here
        var options = WasiOptions.builder().build();
        var wasi = new WasiPreview1(logger, options);
        var wasiHostFunctions = wasi.toHostFunctions();

        var hostFuncList = getHostFunctions(hostEnv.toHostFunctions(), lower(hostFunctions), wasiHostFunctions);
        this.imports = new HostImports(hostFuncList);

        var moduleBuilder = new ManifestModuleMapper(manifest)
                .toModuleBuilder()
                .withLogger(logger)
                .withHostImports(imports);

        this.instance = moduleBuilder.build().instantiate();
    }

    public HostEnv.Log log() {
        return hostEnv.log();
    }

    public HostEnv.Var var() {
        return hostEnv.var();
    }

    public HostEnv.Config config() {
        return hostEnv.config();
    }

    public HostEnv.Memory memory() {
        return hostEnv.memory();
    }

    private HostFunction[] lower(ExtismHostFunction[] fns) {
        var currentPlugin = new CurrentPlugin(this);
        return Arrays.stream(fns).map(fn -> fn.toHostFunction(currentPlugin)).toArray(HostFunction[]::new);
    }

    private static HostFunction[] getHostFunctions(
            HostFunction[] kernelFuncs, HostFunction[] hostFunctions, HostFunction[] wasiHostFunctions) {
        // concat list of host functions
        var hostFuncList = new HostFunction[ kernelFuncs.length + hostFunctions.length + wasiHostFunctions.length];
        System.arraycopy(kernelFuncs, 0, hostFuncList, 0, kernelFuncs.length);
        System.arraycopy(hostFunctions, 0, hostFuncList, kernelFuncs.length, hostFunctions.length);
        System.arraycopy(wasiHostFunctions, 0, hostFuncList, kernelFuncs.length + hostFunctions.length, wasiHostFunctions.length);
        return hostFuncList;
    }

    public byte[] call(String funcName, byte[] input) {
        var func = instance.export(funcName);
        hostEnv.setInput(input);
        var result = func.apply()[0].asInt();
        if (result == 0) {
            return hostEnv.getOutput();
        } else {
            throw new ExtismException("Failed");
        }
    }

}
