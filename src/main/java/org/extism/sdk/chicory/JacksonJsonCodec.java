package org.extism.sdk.chicory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonJsonCodec implements HttpJsonCodec {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RequestMetadata decodeMetadata(byte[] data) {

        JsonNode request = null;
        try {
            request = objectMapper.readTree(data);
        } catch (IOException e) {
            throw new ExtismException(e);
        }

        var method = request.get("method").asText();
        var uri = URI.create(request.get("url").asText());
        var headers = request.get("headers");

        Map<String, String> headersMap = new HashMap<>();
        var fields = headers.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            headersMap.put(entry.getKey(), entry.getValue().asText());
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
        var objectNode = objectMapper.createObjectNode();
        for (var entry : headers.entrySet()) {
            for (var v : entry.getValue()) {
                objectNode.put(entry.getKey(), v);
            }
        }
        try {
            return objectMapper.writeValueAsBytes(objectNode);
        } catch (IOException e) {
            throw new ExtismException(e);
        }
    }
}
