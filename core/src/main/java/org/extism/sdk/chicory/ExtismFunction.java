package org.extism.sdk.chicory;

@FunctionalInterface
public interface ExtismFunction {
    void apply(CurrentPlugin currentPlugin, ExtismValueList args, ExtismValueList returns);
}
