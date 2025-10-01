package org.extism.sdk.chicory.http.it;

import com.dylibso.chicory.log.SystemLogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import junit.framework.TestCase;
import org.gaul.httpbin.HttpBin;
import org.extism.sdk.chicory.ConfigProvider;
import org.extism.sdk.chicory.HostEnv;
import org.extism.sdk.chicory.http.HttpConfig;
import org.extism.sdk.chicory.Kernel;
import org.extism.sdk.chicory.http.ExtismHttpException;
import org.extism.sdk.chicory.http.config.android.AndroidHttpConfig;
import org.extism.sdk.chicory.http.config.generic.GenericHttpConfig;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// Note: lvh.me resolves to localhost.
public class HttpTest extends TestCase {
    public static HttpConfig defaultConfig() {
        return GenericHttpConfig.get();
    }
    public static HttpConfig urlConnectionConfig() {
        return AndroidHttpConfig.get();
    }
    private HttpBin httpBin;

    protected void setUp() throws Exception {
        super.setUp();
        URI httpBinEndpoint = URI.create("http://127.0.0.1:0");
        httpBin = new HttpBin(httpBinEndpoint);
        httpBin.start();
    }

    public void tearDown() throws Exception {
        httpBin.stop();
    }

    public void testInvalidHost() {
        var httpConfig = defaultConfig();
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.lvh.me"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, false, httpConfig, logger);

        URI uri = URI.create("test.lvh.me:" + httpBin.getPort() + "/headers");
        try {
            byte[] response = hostEnv.http().request(
                    "GET",
                    uri,
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("should throw an exception");
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request host is invalid for URI: " + uri, e.getMessage());
        }
    }

    public void testNoAllowedHosts() {
        noAllowedHosts(defaultConfig());
        noAllowedHosts(urlConnectionConfig());
    }

    public void testAllowSingleHost() {
        allowSingleHost(defaultConfig());
        allowSingleHost(urlConnectionConfig());
    }

    public void testAllowHostPattern() {
        allowHostPattern(defaultConfig());
        allowHostPattern(urlConnectionConfig());
    }

    public void testAllowMultiHostPattern() {
        allowMultiHostPattern(defaultConfig());
        allowMultiHostPattern(urlConnectionConfig());
    }

    public void testAllowAnyHost() {
        allowAnyHost(defaultConfig());
        allowAnyHost(urlConnectionConfig());
    }

    public void testResponseHeadersEnabled() {
        responseHeadersEnabled(defaultConfig());
        responseHeadersEnabled(urlConnectionConfig());

    }


    public void noAllowedHosts(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var noAllowedHosts = new String[0];
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), noAllowedHosts, false, httpConfig, logger);

        URI uri = URI.create("http://lvh.me:" + httpBin.getPort() + "/headers");
        try {
            hostEnv.http().request(
                    "GET",
                    uri,
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("Should have thrown an exception");
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request to 'lvh.me' is not allowed", e.getMessage());
        }
    }

    public void allowSingleHost(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"lvh.me"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, false, httpConfig, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://lvh.me:" + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));

        byte[] response2 = hostEnv.http().request(
                "POST",
                URI.create("http://lvh.me:" + httpBin.getPort() + "/post"),
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
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request to 'example.com' is not allowed", e.getMessage());
        }
    }

    public void allowHostPattern(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.lvh.me"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, false, httpConfig, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.lvh.me:" + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        try {
            hostEnv.http().request(
                    "GET",
                    URI.create("http://lvh.me:" + httpBin.getPort() + "/headers"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            fail("Should have thrown an exception");
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request to 'lvh.me' is not allowed", e.getMessage());
        }
    }


    public void allowMultiHostPattern(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.lvh.me", "lvh.me"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, false, httpConfig, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.lvh.me:" + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        response = hostEnv.http().request(
                "GET",
                URI.create("http://lvh.me:" + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));
    }


    public void allowAnyHost(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, false, httpConfig, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.lvh.me:"  + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        response = hostEnv.http().request(
                "GET",
                URI.create("http://lvh.me:" + httpBin.getPort() + "/headers"),
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
        assertEquals(0, hostEnv.http().headers()[0]);
        assertTrue(response.length > 0);
    }

    public void responseHeadersEnabled(HttpConfig httpConfig) {
        var logger = new SystemLogger();

        var anyHost = new String[]{"*"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, true, httpConfig, logger);

        byte[] response = hostEnv.http().request(
                "GET",
                URI.create("http://www.lvh.me:"  + httpBin.getPort() + "/headers"),
                Map.of("X-Custom-Header", "hello"),
                new byte[0]);
        JsonObject responseObject = Json.createReader(new ByteArrayInputStream(response)).readObject();
        assertEquals("hello", responseObject.getJsonObject("headers").getString("X-Custom-Header"));


        response = hostEnv.http().request(
                "GET",
                URI.create("http://lvh.me:" + httpBin.getPort() + "/headers"),
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
        assertTrue(hostEnv.http().headers()[0] > 0);
        assertTrue(response.length > 0);
    }



}
