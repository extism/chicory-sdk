package org.extism.sdk.chicory;

import com.dylibso.chicory.log.SystemLogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpTest extends TestCase {

    public void testNoAllowedHosts() {
        var logger = new SystemLogger();

        var noAllowedHosts = new String[0];
        var hostEnv = new HostEnv(new Kernel(), Map.of(), noAllowedHosts, logger);

        try {
            hostEnv.http().request(
                    "GET",
                    URI.create("http://httpbin.org/headers"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("Should have thrown an exception");
        } catch (ExtismException e) {
            assertEquals("HTTP request to 'httpbin.org' is not allowed", e.getMessage());
        }
    }
    public void testAllowSingleHost() {
        var logger = new SystemLogger();

        var anyHost = new String[]{"httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), Map.of(), anyHost, logger);

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

        try {
            hostEnv.http().request(
                    "GET",
                    URI.create("http://example.com"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("Should have thrown an exception");
        } catch (ExtismException e) {
            assertEquals("HTTP request to 'example.com' is not allowed", e.getMessage());
        }
    }

    public void testAllowHostPattern() {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), Map.of(), anyHost, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        try {
            hostEnv.http().request(
                    "GET",
                    URI.create("http://httpbin.org/headers"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("Should have thrown an exception");
        } catch (ExtismException e) {
            assertEquals("HTTP request to 'httpbin.org' is not allowed", e.getMessage());
        }
    }


    public void testAllowMultiHostPattern() {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org", "httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), Map.of(), anyHost, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        response = hostEnv.http().request(
                "GET",
                URI.create("http://httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));
    }


    public void testAllowAnyHost() {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*"};
        var hostEnv = new HostEnv(new Kernel(), Map.of(), anyHost, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        response = hostEnv.http().request(
                "GET",
                URI.create("http://httpbin.org/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));

        response = hostEnv.http().request(
                "GET",
                URI.create("http://example.com/"),
                Map.of(),
                new byte[0]);

        assertEquals(200, hostEnv.http().statusCode());
        assertTrue(response.length > 0);
    }


}
