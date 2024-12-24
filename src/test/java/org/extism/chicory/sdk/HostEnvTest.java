package org.extism.chicory.sdk;

import com.dylibso.chicory.log.SystemLogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.net.URI;
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

    public void testHttp() {
        var logger = new SystemLogger();
        var hostEnv = new HostEnv(new Kernel(), Map.of(), new String[0], logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));

        byte[] response2 = hostEnv.http().request(
                "POST",
                URI.create("http://httpbin.org/post"),
                Map.of(),
                "hello".getBytes(StandardCharsets.UTF_8));


        JsonObject responseObject2 = Json.createReader(new ByteArrayInputStream(response2)).readObject();
        assertEquals("hello", responseObject2.getString("data"));
    }
}
