package org.extism.sdk.chicory.core;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpJsonCodec {
    interface RequestMetadata {
        String method();
        URI uri();
        Map<String, String> headers();
    }

    RequestMetadata decodeMetadata(byte[] data);
    byte[] encodeHeaders(Map<String, List<String>> headers);
}
