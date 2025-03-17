package org.extism.sdk.chicory.core;

import java.util.Map;

public interface ConfigProvider {
    static ConfigProvider empty() {
        return key -> null;
    }

    static ConfigProvider ofMap(Map<String, String> map) {
        return map::get;
    }

    String get(String key);
}
