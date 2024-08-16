package org.extism.chicory.sdk;

import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;

public class PluginTest
    extends TestCase {

    public void testGreet() {
        var manifest =
                Manifest.Builder
                        .fromUrl("https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                        .build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }

    public void testGreetAoT() {
        var manifest =
                Manifest.Builder
                        .fromUrl("https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                        .withOptions(new Manifest.Options().withAoT())
                        .build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }

}
