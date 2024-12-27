package org.extism.sdk.chicory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class OkHttpClientAdapter implements HttpClientAdapter {
    Response lastResponse;
    OkHttpClient httpClient;

    public OkHttpClient httpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        var reqBuilder = new Request.Builder()
                .url(uri.toString());
        for (var key : headers.keySet()) {
            reqBuilder.header(key, headers.get(key));
        }

        if (requestBody.length == 0) {
            reqBuilder.method(method, null);
        } else {
            reqBuilder.method(method, RequestBody.create(requestBody));
        }

        var req = reqBuilder.build();

        try {
            this.lastResponse = httpClient().newCall(req).execute();
            return lastResponse.body().bytes();
        } catch (IOException e) {
            // FIXME gracefully handle the interruption
            throw new ExtismException(e);
        }
    }

    public int statusCode() {
        return lastResponse == null ? 0 : lastResponse.code();
    }

    @Override
    public Map<String, List<String>> headers() {
        return lastResponse == null ? null : lastResponse.headers().toMultimap();
    }

}
