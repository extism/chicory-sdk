package org.extism.sdk.chicory.core;


import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.ValueType;
import org.extism.sdk.chicory.http.ExtismHttpException;
import org.extism.sdk.chicory.http.HttpClientAdapter;
import org.extism.sdk.chicory.http.HttpJsonCodec;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HttpHostEnvImpl implements HttpHostEnv {
    private final HostPattern[] hostPatterns;
    private final Kernel kernel;
    private final HostEnv.Memory memory;
    Lazy<HttpJsonCodec> jsonCodec;
    Lazy<HttpClientAdapter> clientAdapter;

    public HttpHostEnvImpl(String[] allowedHosts, HttpConfig httpConfig, Kernel kernel, HostEnv.Memory memory) {
        this.kernel = kernel;
        this.memory = memory;
        if (allowedHosts == null) {
            allowedHosts = new String[0];
        }
        this.hostPatterns = new HostPattern[allowedHosts.length];
        for (int i = 0; i < allowedHosts.length; i++) {
            this.hostPatterns[i] = new HostPattern(allowedHosts[i]);
        }
        this.jsonCodec = new Lazy<>(httpConfig.httpJsonCodec);
        this.clientAdapter = new Lazy<>(httpConfig.httpClientAdapter);
    }

    public HttpJsonCodec jsonCodec() {
        return jsonCodec.get();
    }

    public HttpClientAdapter clientAdapter() {
        return clientAdapter.get();
    }

    public long[] request(Instance instance, long... args) {
        var result = new long[1];

        var requestOffset = args[0];
        var bodyOffset = args[1];

        var requestJson = memory.readBytes(requestOffset);
        kernel.free.apply(requestOffset);

        byte[] requestBody;
        if (bodyOffset == 0) {
            requestBody = new byte[0];
        } else {
            requestBody = memory.readBytes(bodyOffset);
            kernel.free.apply(bodyOffset);
        }

        var requestMetadata = jsonCodec().decodeMetadata(requestJson);

        byte[] body = request(
                requestMetadata.method(),
                requestMetadata.uri(),
                requestMetadata.headers(),
                requestBody);

        if (body.length == 0) {
            result[0] = 0;
        } else {
            result[0] = memory.writeBytes(body);
        }

        return result;
    }

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        var host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ExtismHttpException("HTTP request host is invalid for URI: " + uri);
        }
        if (Arrays.stream(hostPatterns).noneMatch(p -> p.matches(host))) {
            throw new ExtismHttpException(String.format("HTTP request to '%s' is not allowed", host));
        }

        return clientAdapter().request(method, uri, headers, requestBody);
    }

    public long[] statusCode(Instance instance, long... args) {
        return new long[]{statusCode()};
    }

    public int statusCode() {
        return clientAdapter().statusCode();
    }

    public long[] headers(Instance instance, long[] longs) {
        var result = new long[1];
        var headers = clientAdapter().headers();
        if (headers == null) {
            return result;
        }
        var bytes = jsonCodec().encodeHeaders(Map.of());
        result[0] = memory.writeBytes(bytes);
        return result;
    }


    public HostFunction[] toHostFunctions() {
        return new HostFunction[]{
                new HostFunction(
                        Kernel.IMPORT_MODULE_NAME,
                        "http_request",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of(ValueType.I64),
                        this::request),
                new HostFunction(
                        Kernel.IMPORT_MODULE_NAME,
                        "http_status_code",
                        List.of(),
                        List.of(ValueType.I32),
                        this::statusCode),
                new HostFunction(
                        Kernel.IMPORT_MODULE_NAME,
                        "http_headers",
                        List.of(),
                        List.of(ValueType.I64),
                        this::headers),

        };
    }
}

final class HostPattern {
    private final String pattern;
    private final boolean exact;

    public HostPattern(String pattern) {
        if (pattern.indexOf('*', 1) != -1) {
            throw new ExtismException("Illegal pattern " + pattern);
        }
        int wildcard = pattern.indexOf('*');
        if (wildcard < 0) {
            this.exact = true;
            this.pattern = pattern;
        } else if (wildcard == 0) {
            this.exact = false;
            this.pattern = pattern.substring(1);
        } else {
            throw new ExtismException("Illegal pattern " + pattern);
        }
    }

    public boolean matches(String host) {
        if (exact) {
            return host.equals(pattern);
        } else {
            return host.endsWith(pattern);
        }
    }
}

final class Lazy<T> {
    final Supplier<T> supplier;
    T t;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (t == null) {
            try {
                t = supplier.get();
            } catch (NoClassDefFoundError error) {
                throw new ConfigurationException(
                        "Http has not been configured properly. " +
                                "Verify you have added a dependency to the JSON deserializer (default: Jackson Databind) " +
                                "and you have have configure the HTTP client properly (default: java.net.http.HttpClient)", error);
            }
        }
        return t;
    }
}
