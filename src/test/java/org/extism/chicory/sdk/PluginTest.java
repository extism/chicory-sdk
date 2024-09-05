package org.extism.chicory.sdk;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PluginTest extends TestCase {

    public void testGreet() {
        var manifest =
                Manifest.ofWasms(
                        ManifestWasm.fromUrl(
                                "https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                        .build()).build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }

    public void testGreetAoT() {
        var manifest =
                Manifest.ofWasms(
                        ManifestWasm.fromUrl(
                                "https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                                .withName("greet")
                                .build())
                        .withOptions(new Manifest.Options().withAoT())
                        .build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }


    public void testCircularDeps() throws IOException {
        InputStream is1 = this.getClass().getResourceAsStream("/circular-import-1.wasm");
        InputStream is2 = this.getClass().getResourceAsStream("/circular-import-2.wasm");
        InputStream is3 = this.getClass().getResourceAsStream("/circular-import-main.wasm");
        var manifest =
                Manifest.ofWasms(
                        ManifestWasm.fromBytes(is1.readAllBytes()).withName("env-1").build(),
                        ManifestWasm.fromBytes(is2.readAllBytes()).withName("env-2").build(),
                        ManifestWasm.fromBytes(is3.readAllBytes()).withName("main").build()).build();
        var plugin = Plugin.ofManifest(manifest).build();
        byte[] result = plugin.call("real_do_expr", new byte[0]);
        System.out.println(Arrays.toString(result));
    }

}
