package org.extism.sdk.chicory.core;

import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import junit.framework.TestCase;

import java.util.List;

public class ExtismHostFunctionTest extends TestCase {
    public void testFunction() {
        var f = ExtismHostFunction.of("myfunc", List.of(), List.of(),
                (CurrentPlugin p, ExtismValueList args, ExtismValueList results) ->
                        p.log().log(LogLevel.INFO, "hello world"));

        var manifest = Manifest.ofWasms(
                ManifestWasm.fromUrl(
                                "https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                        .build()).build();
        var plugin = Plugin.ofManifest(manifest).withLogger(new SystemLogger()).build();


        HostFunction hostFunction = f.asHostFunction();
        f.bind(new CurrentPlugin(plugin));
        Instance instance = null;
        long[] args = null;
        hostFunction.handle().apply(instance, args);
    }
}
