package org.extism.sdk.chicory;

import java.util.Map;

public interface ConfigProvider {
    static ConfigProvider Empty() {
        return key -> null;
    }

    static ConfigProvider ofMap(Map<String, String> map) {
        return map::get;
    }

    String get(String key);
}
