package org.extism.sdk.chicory.core;

import org.extism.sdk.chicory.http.HttpClientAdapter;
import org.extism.sdk.chicory.http.HttpJsonCodec;
import org.extism.sdk.chicory.http.HttpUrlConnectionClientAdapter;
import org.extism.sdk.chicory.http.JdkHttpClientAdapter;

import java.util.Objects;
import java.util.function.Supplier;

public class HttpConfig {
    /**
     * Use {@link JdkHttpClientAdapter} for the HTTP client adapter.
     * Recommended on recent Java versions.
     */
    public static HttpConfig defaultConfig() {
        return HttpConfig.builder()
                .withClientAdapter(JdkHttpClientAdapter::new)
                .withJsonCodec(JacksonJsonCodec::new).build();
    }

    /**
     * Use {@link HttpUrlConnectionClientAdapter} for the HTTP client adapter.
     * Recommended for Android.
     */
    public static HttpConfig urlConnectionConfig() {
        return HttpConfig.builder()
                .withClientAdapter(HttpUrlConnectionClientAdapter::new)
                .withJsonCodec(JakartaJsonCodec::new).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Supplier<HttpJsonCodec> httpJsonCodec;
        Supplier<HttpClientAdapter> httpClientAdapter;

        private Builder() {}

        public Builder withJsonCodec(Supplier<HttpJsonCodec> httpJsonCodecFactory) {
            this.httpJsonCodec = httpJsonCodecFactory;
            return this;
        }

        public Builder withClientAdapter(Supplier<HttpClientAdapter> httpClientAdapterFactory) {
            this.httpClientAdapter = httpClientAdapterFactory;
            return this;
        }

        public HttpConfig build() {
            Objects.requireNonNull(httpJsonCodec, "httpJsonCodec is required");
            Objects.requireNonNull(httpClientAdapter, "httpClientAdapter is required");
            return new HttpConfig(httpJsonCodec, httpClientAdapter);
        }
    }


    Supplier<HttpJsonCodec> httpJsonCodec;
    Supplier<HttpClientAdapter> httpClientAdapter;

    public HttpConfig(Supplier<HttpJsonCodec> httpJsonCodec, Supplier<HttpClientAdapter> httpClientAdapter) {
        this.httpJsonCodec = httpJsonCodec;
        this.httpClientAdapter = httpClientAdapter;
    }

}
