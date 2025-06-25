package org.extism.sdk.chicory.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpClientAdapter {
    byte[] request(String method, URI url, Map<String, String> headers, byte[] requestBody);
    int statusCode();
    Map<String, List<String>> headers();
}
