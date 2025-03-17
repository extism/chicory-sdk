package org.extism.sdk.chicory.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class JdkHttpClientAdapter implements HttpClientAdapter {
    HttpResponse<byte[]> lastResponse;
    HttpClient httpClient;

    public HttpClient httpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newHttpClient();
        }
        return httpClient;
    }

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        var reqBuilder = HttpRequest.newBuilder()
                .uri(uri);
        for (var key : headers.keySet()) {
            reqBuilder.header(key, headers.get(key));
        }

        if (requestBody.length == 0) {
            reqBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            reqBuilder.method(method, HttpRequest.BodyPublishers.ofByteArray(requestBody));
        }

        var req = reqBuilder.build();

        try {
            this.lastResponse = httpClient().send(req, HttpResponse.BodyHandlers.ofByteArray());
            return lastResponse.body();
        } catch (IOException | InterruptedException e) {
            // FIXME gracefully handle the interruption
            throw new ExtismHttpException(e);
        }
    }

    public int statusCode() {
        return lastResponse == null ? 0 : lastResponse.statusCode();
    }

    public Map<String, java.util.List<String>> headers() {
        return lastResponse == null ? null : lastResponse.headers().map();
    }

}
