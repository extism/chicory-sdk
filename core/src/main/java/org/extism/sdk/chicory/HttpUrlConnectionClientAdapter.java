package org.extism.sdk.chicory;

import java.net.URI;
import java.util.Map;

/**
 * @deprecated instead use <code>org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter</code>
 *             in <code>org.extism.sdk:http-client-urlconnection</code>
 */
@Deprecated(forRemoval = true)
public class HttpUrlConnectionClientAdapter implements HttpClientAdapter {

    public HttpUrlConnectionClientAdapter() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter` " +
                        "in `org.extism.sdk:http-client-urlconnection`");
    }

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter` " +
                        "in `org.extism.sdk:http-client-urlconnection`");
    }

    public int statusCode() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter` " +
                        "in `org.extism.sdk:http-client-urlconnection`");
    }

    public Map<String, java.util.List<String>> headers() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter` " +
                        "in `org.extism.sdk:http-client-urlconnection`");
    }

}
