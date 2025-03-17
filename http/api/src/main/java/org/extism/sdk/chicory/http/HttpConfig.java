package org.extism.sdk.chicory.http;

import java.util.Objects;
import java.util.function.Supplier;

public class HttpConfig {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Supplier<HttpJsonCodec> httpJsonCodec;
        Supplier<HttpClientAdapter> httpClientAdapter;

        private Builder() {
        }

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

    public Supplier<HttpJsonCodec> httpJsonCodec() {
        return httpJsonCodec;
    }

    public Supplier<HttpClientAdapter> httpClientAdapter() {
        return httpClientAdapter;
    }

}
