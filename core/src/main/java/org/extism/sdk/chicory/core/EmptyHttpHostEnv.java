package org.extism.sdk.chicory.core;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.ValueType;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class EmptyHttpHostEnv implements HttpHostEnv {
    @Override
    public long[] request(Instance instance, long... args) {
        return new long[0];
    }

    @Override
    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        return new byte[0];
    }

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public long[] statusCode(Instance instance, long... args) {
        return new long[0];
    }

    @Override
    public long[] headers(Instance instance, long[] longs) {
        return new long[0];
    }

    @Override
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
