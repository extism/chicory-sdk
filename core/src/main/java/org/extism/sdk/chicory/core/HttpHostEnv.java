package org.extism.sdk.chicory.core;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

import java.net.URI;
import java.util.Map;

public interface HttpHostEnv {
    long[] request(Instance instance, long... args);

    byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody);

    int statusCode();

    long[] statusCode(Instance instance, long... args);

    long[] headers(Instance instance, long[] longs);

    HostFunction[] toHostFunctions();
}

