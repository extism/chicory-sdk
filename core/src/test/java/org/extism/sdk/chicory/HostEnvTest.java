package org.extism.sdk.chicory;

import com.dylibso.chicory.log.SystemLogger;
import junit.framework.TestCase;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HostEnvTest extends TestCase {
    public void testShowcase() {
        var logger = new SystemLogger();

        var config = Map.of("key", "value");
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.ofMap(config), new String[0], false, null, logger);

        assertEquals(hostEnv.config().get("key"), "value");

        byte[] bytes = "extism".getBytes(StandardCharsets.UTF_8);
        hostEnv.var().set("test", bytes);
        assertSame(hostEnv.var().get("test"), bytes);

        hostEnv.log().log(LogLevel.INFO, "hello world");

        int size = 100;
        long ptr = hostEnv.memory().alloc(size);
        assertEquals(hostEnv.memory().length(ptr), size);
    }

    public void testHttpThrows() {
        var logger = new SystemLogger();

        var config = Map.of("key", "value");
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.ofMap(config), new String[0], false, null, logger);
        try {
            hostEnv.http().request("POST", URI.create("https://www.example.com"), Map.of(), new byte[0]);
            fail("It should throw an ExtismConfigurationException");
        } catch (ExtismConfigurationException ex) {
            // expected
        }

    }
}
