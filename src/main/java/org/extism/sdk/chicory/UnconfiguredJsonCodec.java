package org.extism.sdk.chicory;

import java.util.List;
import java.util.Map;

public final class UnconfiguredJsonCodec implements HttpJsonCodec {

    @Override
    public RequestMetadata decodeMetadata(byte[] data) {
        throw new ConfigurationException(
                "If you need Http support, you should configure a JsonCodec");
    }

    @Override
    public byte[] encodeHeaders(Map<String, List<String>> headers) {
        throw new ConfigurationException(
                "If you need Http support, you should configure a JsonCodec");
    }
}
