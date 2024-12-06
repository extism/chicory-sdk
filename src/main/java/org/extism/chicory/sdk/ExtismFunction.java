package org.extism.chicory.sdk;

@FunctionalInterface
public interface ExtismFunction {
    void apply(CurrentPlugin currentPlugin, ExtismValueList args, ExtismValueList returns);
}
