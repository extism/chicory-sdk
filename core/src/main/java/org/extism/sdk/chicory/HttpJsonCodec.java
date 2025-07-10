package org.extism.sdk.chicory;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.HttpJsonCodec</code>
 *             in <code>org.extism.sdk:http-api</code>
 */
@Deprecated(forRemoval = true)
public interface HttpJsonCodec {
    @Deprecated(forRemoval = true)
    interface RequestMetadata {
        String method();
        URI uri();
        Map<String, String> headers();
    }

    RequestMetadata decodeMetadata(byte[] data);
    byte[] encodeHeaders(Map<String, List<String>> headers);
}
