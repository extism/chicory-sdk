package org.extism.sdk.chicory;

import java.util.Objects;

public class HttpConfig {
    public static HttpConfig empty() {
        return HttpConfig.builder()
                .withClientAdapter(new UnconfiguredHttpClientAdapter())
                .withJsonCodec(new UnconfiguredJsonCodec())
                .build();
    }
    /**
     * Use {@link JdkHttpClientAdapter} for the HTTP client adapter.
     * Recommended on recent Java versions.
     */
    public static HttpConfig defaultConfig() {
        return HttpConfig.builder()
                .withClientAdapter(new JdkHttpClientAdapter())
                .withJsonCodec(new JacksonJsonCodec()).build();
    }

    /**
     * Use {@link HttpUrlConnectionClientAdapter} for the HTTP client adapter.
     * Recommended for Android.
     */
    public static HttpConfig urlConnectionConfig() {
        return HttpConfig.builder()
                .withClientAdapter(new HttpUrlConnectionClientAdapter())
                .withJsonCodec(new JakartaJsonCodec()).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        HttpJsonCodec httpJsonCodec;
        HttpClientAdapter httpClientAdapter;

        private Builder() {}

        public Builder withJsonCodec(HttpJsonCodec httpJsonCodec) {
            this.httpJsonCodec = httpJsonCodec;
            return this;
        }

        public Builder withClientAdapter(HttpClientAdapter httpClientAdapter) {
            this.httpClientAdapter = httpClientAdapter;
            return this;
        }

        public HttpConfig build() {
            Objects.requireNonNull(httpJsonCodec, "httpJsonCodec is required");
            Objects.requireNonNull(httpClientAdapter, "httpClientAdapter is required");
            return new HttpConfig(httpJsonCodec, httpClientAdapter);
        }
    }


    HttpJsonCodec httpJsonCodec;
    HttpClientAdapter httpClientAdapter;

    public HttpConfig(HttpJsonCodec httpJsonCodec, HttpClientAdapter httpClientAdapter) {
        this.httpJsonCodec = httpJsonCodec;
        this.httpClientAdapter = httpClientAdapter;
    }

}
