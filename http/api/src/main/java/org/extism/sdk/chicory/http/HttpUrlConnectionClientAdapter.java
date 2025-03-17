package org.extism.sdk.chicory.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpUrlConnectionClientAdapter implements HttpClientAdapter {
    Map<String, List<String>> lastResponseHeaders = Map.of();
    private byte[] lastBody =  new byte[0];
    private int lastResponseCode = 0;

    public byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
        HttpURLConnection conn = null;
        try {
            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            for (var key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }

            if (requestBody.length != 0) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody);
                    os.flush();
                }
            }


            InputStream is;
            lastResponseHeaders = conn.getHeaderFields();
            lastResponseCode = conn.getResponseCode();

            if (100 <= lastResponseCode && lastResponseCode <= 399) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            lastBody = is.readAllBytes();

            return lastBody;
        } catch (IOException e) {
            // FIXME gracefully handle the interruption
            throw new ExtismHttpException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public int statusCode() {
        return lastResponseCode;
    }

    public Map<String, java.util.List<String>> headers() {
        return lastResponseHeaders;
    }

}
