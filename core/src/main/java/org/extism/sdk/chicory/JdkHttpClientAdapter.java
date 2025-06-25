package org.extism.sdk.chicory;

import java.net.URI;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter</code>
 *             in <code>org.extism.sdk:http-client-javanet</code>
 */
@Deprecated(forRemoval = true)
public class JdkHttpClientAdapter implements HttpClientAdapter {

    public JdkHttpClientAdapter() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter` " +
                        "in `org.extism.sdk:http-client-javanet`");
    }

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter` " +
                        "in `org.extism.sdk:http-client-javanet`");
    }

    public int statusCode() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter` " +
                        "in `org.extism.sdk:http-client-javanet`");
    }

    public Map<String, java.util.List<String>> headers() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter` " +
                        "in `org.extism.sdk:http-client-javanet`");
    }

}
