package org.extism.sdk.chicory.core;

import com.dylibso.chicory.runtime.HostFunction;

import java.net.URI;
import java.util.Map;

class EmptyHttpHostEnv implements HostEnv.Http {
    @Override
    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        throw new ConfigurationException("Http has not been configured properly. " +
                "Verify you have added a dependency to the JSON deserializer (default: Jackson Databind) " +
                "and you have have configure the HTTP client properly (default: java.net.http.HttpClient)");
    }

    @Override
    public int statusCode() {
        throw new ConfigurationException("Http has not been configured properly. " +
                "Verify you have added a dependency to the JSON deserializer (default: Jackson Databind) " +
                "and you have have configure the HTTP client properly (default: java.net.http.HttpClient)");
    }

    @Override
    public HostFunction[] toHostFunctions() {
        return new HostFunction[0];
    }

}
