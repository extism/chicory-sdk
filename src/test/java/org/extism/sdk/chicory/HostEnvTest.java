package org.extism.sdk.chicory;

import com.dylibso.chicory.log.SystemLogger;
import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HostEnvTest extends TestCase {
    public void testShowcase() {
        var logger = new SystemLogger();

        var config = Map.of("key", "value");
        var hostEnv = new HostEnv(new Kernel(), config, new String[0], logger);

        assertEquals(hostEnv.config().get("key"), "value");

        byte[] bytes = "extism".getBytes(StandardCharsets.UTF_8);
        hostEnv.var().set("test", bytes);
        assertSame(hostEnv.var().get("test"), bytes);

        hostEnv.log().log(LogLevel.INFO, "hello world");

        int size = 100;
        long ptr = hostEnv.memory().alloc(size);
        assertEquals(hostEnv.memory().length(ptr), size);
    }
}
