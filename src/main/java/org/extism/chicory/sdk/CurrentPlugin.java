package org.extism.chicory.sdk;

public class CurrentPlugin {
    private final Plugin plugin;

    public CurrentPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public HostEnv.Log log() {
        return plugin.log();
    }

    public HostEnv.Memory memory() {
        return plugin.memory();
    }

}
