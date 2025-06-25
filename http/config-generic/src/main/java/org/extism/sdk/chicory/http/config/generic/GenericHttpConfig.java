package org.extism.sdk.chicory.http.config.generic;

import org.extism.sdk.chicory.http.HttpConfig;
import org.extism.sdk.chicory.http.client.javanet.JavaNetHttpClientAdapter;
import org.extism.sdk.chicory.http.jackson.JacksonJsonCodec;

public final class GenericHttpConfig {
    public static HttpConfig get() {
        return HttpConfig.builder()
                .withClientAdapter(JavaNetHttpClientAdapter::new)
                .withJsonCodec(JacksonJsonCodec::new).build();
    }
}
