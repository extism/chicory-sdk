package org.extism.sdk.chicory;

import java.util.List;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.jakarta.JakartaJsonCodec</code>
 *             in <code>org.extism.sdk:http-json-jakarta</code>
 */
@Deprecated(forRemoval = true)
public class JakartaJsonCodec implements HttpJsonCodec {

    public JakartaJsonCodec() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jakarta.JakartaJsonCodec` " +
                        "in `org.extism.sdk:http-json-jakarta`");
    }

    @Override
    public RequestMetadata decodeMetadata(byte[] data) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jakarta.JakartaJsonCodec` " +
                        "in `org.extism.sdk:http-json-jakarta`");

    }

    public byte[] encodeHeaders(Map<String, List<String>> headers) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jakarta.JakartaJsonCodec` " +
                        "in `org.extism.sdk:http-json-jakarta`");
    }
}
