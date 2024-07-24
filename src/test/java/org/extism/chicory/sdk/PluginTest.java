package org.extism.chicory.sdk;

import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;

public class PluginTest
    extends TestCase
{

    public void testGreet()
    {
        var manifest = Manifest.fromUrl("https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm");
        var plugin = new Plugin(manifest);
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }
}
