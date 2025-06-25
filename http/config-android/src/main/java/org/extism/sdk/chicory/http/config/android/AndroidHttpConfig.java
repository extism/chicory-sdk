package org.extism.sdk.chicory.http.config.android;

import org.extism.sdk.chicory.http.HttpConfig;
import org.extism.sdk.chicory.http.client.urlconnection.HttpUrlConnectionClientAdapter;
import org.extism.sdk.chicory.http.jackson.JacksonJsonCodec;

public final class AndroidHttpConfig {
    public static HttpConfig get() {
        return HttpConfig.builder()
                .withClientAdapter(HttpUrlConnectionClientAdapter::new)
                .withJsonCodec(JacksonJsonCodec::new).build();
    }
}
