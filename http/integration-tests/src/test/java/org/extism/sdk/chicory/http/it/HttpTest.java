package org.extism.sdk.chicory.http.it;

import com.dylibso.chicory.log.SystemLogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.extism.sdk.chicory.core.ConfigProvider;
import org.extism.sdk.chicory.core.ExtismException;
import org.extism.sdk.chicory.core.HostEnv;
import org.extism.sdk.chicory.core.HttpConfig;
import org.extism.sdk.chicory.core.Kernel;
import org.extism.sdk.chicory.http.ExtismHttpException;
import org.extism.sdk.chicory.http.HttpUrlConnectionClientAdapter;
import org.extism.sdk.chicory.http.JdkHttpClientAdapter;
import org.extism.sdk.chicory.http.jackson.JacksonJsonCodec;
import org.extism.sdk.chicory.http.jakarta.JakartaJsonCodec;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class HttpTest extends TestCase {

    public static HttpConfig defaultConfig() {
        return HttpConfig.builder()
                .withClientAdapter(JdkHttpClientAdapter::new)
                .withJsonCodec(JacksonJsonCodec::new).build();
    }

    /**
     * Use {@link HttpUrlConnectionClientAdapter} for the HTTP client adapter.
     * Recommended for Android.
     */
    public static HttpConfig urlConnectionConfig() {
        return HttpConfig.builder()
                .withClientAdapter(HttpUrlConnectionClientAdapter::new)
                .withJsonCodec(JakartaJsonCodec::new).build();
    }


    public void testInvalidHost() {
        var httpConfig = defaultConfig();
        var logger = new SystemLogger();

        var anyHost = new String[]{"*.httpbin.org"};
        var hostEnv = new HostEnv(new Kernel(), ConfigProvider.empty(), anyHost, httpConfig, logger);

        try {
            byte[] response = hostEnv.http().request(
                    "GET",
                    URI.create("httpbin.org/headers"),
                    Map.of("X-Custom-Header", "hello"),
                    new byte[0]);
            Assert.fail("should throw an exception");
        } catch (ExtismHttpException e) {
            assertEquals("HTTP request host is invalid for URI: httpbin.org/headers", e.getMessage());
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
            Assert.fail("Should have thrown an exception");
        } catch (ExtismHttpException e) {
            Assert.assertEquals("HTTP request to 'httpbin.org' is not allowed", e.getMessage());
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
            Assert.fail("Should have thrown an exception");
        } catch (ExtismHttpException e) {
            Assert.assertEquals("HTTP request to 'example.com' is not allowed", e.getMessage());
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
            Assert.fail("Should have thrown an exception");
        } catch (ExtismHttpException e) {
            Assert.assertEquals("HTTP request to 'httpbin.org' is not allowed", e.getMessage());
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

        Assert.assertEquals(200, hostEnv.http().statusCode());
        Assert.assertTrue(response.length > 0);
    }


}
