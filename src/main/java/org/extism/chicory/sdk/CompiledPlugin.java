package org.extism.chicory.sdk;

/**
 * A plugin that has been already processed, but not yet instantiated.
 * Every instance is independent and shares no state.
 */
public class CompiledPlugin {
    private final Linker linker;

    CompiledPlugin(Linker linker) {
        this.linker = linker;
    }

    public Plugin instantiate() {
        return linker.link();
    }
}
