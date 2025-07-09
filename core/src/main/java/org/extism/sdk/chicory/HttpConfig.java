package org.extism.sdk.chicory;

import java.util.Objects;
import java.util.function.Supplier;

@Deprecated(forRemoval = true)
public class HttpConfig {
    /**
     * @deprecated instead use <code>org.extism.sdk.chicory.http.config.generic.GenericHttpConfig</code>
     *             in <code>org.extism.sdk:http-config-generic</code>
     */
    @Deprecated(forRemoval = true)
    public static HttpConfig defaultConfig() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.config.generic.GenericHttpConfig` " +
                        "in `org.extism.sdk:http-config-generic`");
    }

    /**
     * @deprecated instead use <code>org.extism.sdk.chicory.http.config.android.AndroidHttpConfig</code>
     *             in <code>org.extism.sdk:http-config-android</code>
     */
    @Deprecated(forRemoval = true)
    public static HttpConfig urlConnectionConfig() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.config.android.AndroidHttpConfig` " +
                        "in `org.extism.sdk:http-config-android`");
    }

    @Deprecated(forRemoval = true)
    public static Builder builder() {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.HttpConfig.builder()` " +
                        "in `org.extism.sdk:http-api`");

    }

    @Deprecated(forRemoval = true)
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

    @Deprecated(forRemoval = true)
    public HttpConfig(Supplier<HttpJsonCodec> httpJsonCodec, Supplier<HttpClientAdapter> httpClientAdapter) {
        throw new UnsupportedOperationException(
                "Deprecated for removal, instead use `org.extism.sdk.chicory.http.config.generic.GenericHttpConfig` " +
                        "in `org.extism.sdk:http-config-generic`");
    }

}
