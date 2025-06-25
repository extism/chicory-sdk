package org.extism.sdk.chicory;

import com.dylibso.chicory.wasi.WasiOptions;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class LogTest extends TestCase {
    public void testLogLevel() throws IOException {
        var buf = new ByteArrayOutputStream();
        var wasm = ManifestWasm.fromBytes(this.getClass().getResourceAsStream("/log/log.wasm").readAllBytes()).build();
        var manifest = Manifest.ofWasms(wasm).withOptions(new Manifest.Options().withWasi(WasiOptions.builder().build())).build();

        SimpleFormatter fmt = new SimpleFormatter();
        StreamHandler sh = new StreamHandler(buf, fmt);
        sh.setLevel(Level.ALL);
        Logger.getLogger("chicory").addHandler(sh);


        var plugin = Plugin.ofManifest(manifest).build();
        plugin.log().setLogLevel(LogLevel.WARN);
        plugin.call("run_test", new byte[0]);

        sh.flush();
        var result = buf.toString();

        assertTrue(result.contains("this is a warning log"));
        assertTrue(result.contains("this is an error log"));
        assertFalse(result.contains("this is a trace log"));
        assertFalse(result.contains("this is a debug log"));
        assertFalse(result.contains("this is an info log"));

    }
}
