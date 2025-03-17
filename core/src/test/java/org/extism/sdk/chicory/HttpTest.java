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

    public void testInvalidHost() {
        var httpConfig = HttpConfig.defaultConfig();
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

        try {
            byte[] response = hostEnv.http().request(
                    "GET",
                    URI.create("httpbin.org/headers"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("should throw an exception");
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request host is invalid for URI: httpbin.org/headers", e.getMessage());
        }
    }

    public void testNoAllowedHosts() {
        noAllowedHosts(HttpConfig.defaultConfig());
        noAllowedHosts(HttpConfig.urlConnectionConfig());
    }

    public void testAllowSingleHost() {
        allowSingleHost(HttpConfig.defaultConfig());
        allowSingleHost(HttpConfig.urlConnectionConfig());
    }

    public void testAllowHostPattern() {
        allowHostPattern(HttpConfig.defaultConfig());
        allowHostPattern(HttpConfig.urlConnectionConfig());
    }

    public void testAllowMultiHostPattern() {
        allowMultiHostPattern(HttpConfig.defaultConfig());
        allowMultiHostPattern(HttpConfig.urlConnectionConfig());
    }

    public void testAllowAnyHost() {
        allowAnyHost(HttpConfig.defaultConfig());
        allowAnyHost(HttpConfig.urlConnectionConfig());
    }

    public void noAllowedHosts(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var noAllowedHosts = new String[0];
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), noAllowedHosts, httpConfig, logger);

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

    public void allowSingleHost(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

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
                Map.of("Content-Type", "text/plain"),
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

    public void allowHostPattern(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

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


    public void allowMultiHostPattern(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org", "httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

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


    public void allowAnyHost(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

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
