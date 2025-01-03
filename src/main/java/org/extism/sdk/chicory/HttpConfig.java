package org.extism.sdk.chicory;

public class HttpConfig {
    /**
     * Use {@link JdkHttpClientAdapter} for the HTTP client adapter.
     * Recommended on recent Java versions.
     */
    public static HttpConfig defaultConfig() {
        return new HttpConfig().withClientAdapter(new JdkHttpClientAdapter()).withJsonCodec(new JakartaJsonCodec());
    }

    /**
     * Use {@link HttpUrlConnectionClientAdapter} for the HTTP client adapter.
     * Recommended for Android.
     */
    public static HttpConfig urlConnectionConfig() {
        return new HttpConfig().withClientAdapter(new HttpUrlConnectionClientAdapter()).withJsonCodec(new JakartaJsonCodec());
    }

    HttpJsonCodec httpJsonCodec;
    HttpClientAdapter httpClientAdapter;

    public HttpConfig withJsonCodec(HttpJsonCodec httpJsonCodec) {
        this.httpJsonCodec = httpJsonCodec;
        return this;
    }

    public HttpConfig withClientAdapter(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
        return this;
    }
}
