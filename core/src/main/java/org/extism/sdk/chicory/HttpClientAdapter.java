package org.extism.sdk.chicory;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.HttpClientAdapter</code>
 *             in <code>org.extism.sdk:http-api</code>
 */
@Deprecated(forRemoval = true)
public interface HttpClientAdapter {
    byte[] request(String method, URI url, Map<String, String> headers, byte[] requestBody);
    int statusCode();
    Map<String, List<String>> headers();
}
