package org.extism.sdk.chicory.core;

@FunctionalInterface
public interface ExtismFunction {
    void apply(CurrentPlugin currentPlugin, ExtismValueList args, ExtismValueList returns);
}
