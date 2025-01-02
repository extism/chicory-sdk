package org.extism.sdk.chicory;

public class HttpConfig {
    public static HttpConfig defaultConfig() {
        return new HttpConfig().withClientAdapter(new JdkHttpClientAdapter()).withJsonCodec(new JakartaJsonCodec());
    }

    public static HttpConfig androidConfig() {
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
