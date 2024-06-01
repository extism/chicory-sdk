package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Plugin {
    private final Manifest manifest;
    private final Instance instance;
    private final HostImports imports;
    private final Kernel kernel;

    public Plugin(Manifest manifest) {
        this(manifest, new HostFunction[]{}, null);
    }

    public Plugin(Manifest manifest, HostFunction[] hostFunctions, Logger logger) {
        this.kernel = new Kernel();
        this.manifest = manifest;

        if (logger == null) {
            logger = new SystemLogger();
        }

        // TODO: Expand WASI Support here
        var options = WasiOptions.builder().build();
        var wasi = new WasiPreview1(logger, options);
        var wasiHostFunctions = wasi.toHostFunctions();

        // concat list of host functions
        var kernelFuncs = kernel.toHostFunctions();
        var hostFuncList = new HostFunction[hostFunctions.length + kernelFuncs.length + wasiHostFunctions.length];
        System.arraycopy(kernelFuncs, 0, hostFuncList, 0, kernelFuncs.length);
        System.arraycopy(hostFunctions, 0, hostFuncList, kernelFuncs.length, hostFunctions.length);
        System.arraycopy(wasiHostFunctions, 0, hostFuncList, kernelFuncs.length + hostFunctions.length, wasiHostFunctions.length);
        this.imports = new HostImports(hostFuncList);

        var wasm = this.manifest.wasms[0];
        Module.Builder builder;
        if (wasm instanceof ManifestWasmPath) {
            builder = Module.builder(((ManifestWasmPath) wasm).path);
        } else if (wasm instanceof ManifestWasmUrl) {
            try {
                var url = new URL(((ManifestWasmUrl) wasm).url);
                var wasmInputStream = url.openStream();
                builder = Module.builder(wasmInputStream);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (wasm instanceof ManifestWasmFile) {
            builder = Module.builder(((ManifestWasmFile) wasm).filePath);
        } else {
            throw new RuntimeException("We don't know what to do with this manifest");
        }

        this.instance = builder.withLogger(logger).build()
                .withHostImports(imports)
                .instantiate();
    }

    public byte[] call(String funcName, byte[] input) {
        var func = instance.export(funcName);
        kernel.setInput(input);
        var result = func.apply()[0].asInt();
        if (result == 0) {
            return kernel.getOutput();
        } else {
            throw new RuntimeException("Failed");
        }
    }

}
