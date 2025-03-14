package org.extism.sdk.chicory;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class UnconfiguredHttpClientAdapter implements HttpClientAdapter{
    @Override
    public byte[] request(String method, URI url, Map<String, String> headers, byte[] requestBody) {
        throw new ConfigurationException(
                "If you need Http support, you should configure a HttpClientAdapter");
    }

    @Override
    public int statusCode() {
        throw new ConfigurationException(
                "If you need Http support, you should configure a HttpClientAdapter");    }

    @Override
    public Map<String, List<String>> headers() {
        throw new ConfigurationException(
                "If you need Http support, you should configure a HttpClientAdapter");    }
}
