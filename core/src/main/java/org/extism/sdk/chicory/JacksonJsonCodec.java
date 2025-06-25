package org.extism.sdk.chicory;

import java.util.List;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.jackson.JacksonJsonCodec</code>
 *             in <code>org.extism.sdk:http-json-jackson</code>
 */
@Deprecated(forRemoval = true)
public class JacksonJsonCodec implements HttpJsonCodec {

    public JacksonJsonCodec() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jackson.JacksonJsonCodec` " +
                        "in `org.extism.sdk:http-json-jackson`");

    }

    @Override
    public RequestMetadata decodeMetadata(byte[] data) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jackson.JacksonJsonCodec` " +
                        "in `org.extism.sdk:http-json-jackson`");

    }

    public byte[] encodeHeaders(Map<String, List<String>> headers) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.jackson.JacksonJsonCodec` " +
                        "in `org.extism.sdk:http-json-jackson`");
    }
}
