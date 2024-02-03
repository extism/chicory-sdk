package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Module;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Plugin {
    private final Manifest manifest;
    private final Instance instance;
    private final HostImports imports;
    private final Kernel kernel;

    public Plugin(Manifest manifest) {
        this(manifest, new HostFunction[]{});
    }

    public Plugin(Manifest manifest, HostFunction[] hostFunctions) {
        this.kernel = new Kernel();
        this.manifest = manifest;

        // concat list of host functions
        var kernelFuncs = kernel.toHostFunctions();
        var hostFuncList = new HostFunction[hostFunctions.length + kernelFuncs.length];
        System.arraycopy(kernelFuncs, 0, hostFuncList, 0, kernelFuncs.length);
        System.arraycopy(hostFunctions, 0, hostFuncList, kernelFuncs.length, hostFunctions.length);
        this.imports = new HostImports(hostFuncList);

        var wasm = this.manifest.wasms[0];
        if (wasm instanceof ManifestWasmPath) {
            this.instance = Module.builder(((ManifestWasmPath) wasm).path).build().instantiate(imports);
        } else if (wasm instanceof ManifestWasmUrl) {
            try {
                var url = new URL(((ManifestWasmUrl) wasm).url);
                var wasmInputStream = url.openStream();
                this.instance = Module.builder(wasmInputStream).build().instantiate(imports);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("We don't know what to do with this manifest");
        }
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
