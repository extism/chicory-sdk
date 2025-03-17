package org.extism.sdk.chicory.core;

import jakarta.json.Json;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JakartaJsonCodec implements HttpJsonCodec {

    @Override
    public RequestMetadata decodeMetadata(byte[] data) {

        var request = Json.createReader(new ByteArrayInputStream(data))
                .readObject();

        var method = request.getJsonString("method").getString();
        var uri = URI.create(request.getJsonString("url").getString());
        var headers = request.getJsonObject("headers");

        Map<String, String> headersMap = new HashMap<>();
        for (var key : headers.keySet()) {
            headersMap.put(key, headers.getString(key));
        }
        return new RequestMetadata() {
            @Override
            public String method() {
                return method;
            }

            @Override
            public URI uri() {
                return uri;
            }

            @Override
            public Map<String, String> headers() {
                return headersMap;
            }
        };
    }

    public byte[] encodeHeaders(Map<String, List<String>> headers) {
        // FIXME duplicated headers are effectively overwriting duplicate values!
        var objBuilder = Json.createObjectBuilder();
        for (var entry : headers.entrySet()) {
            for (var v : entry.getValue()) {
                objBuilder.add(entry.getKey(), v);
            }
        }
        return objBuilder.build().toString().getBytes(StandardCharsets.UTF_8);
    }
}
