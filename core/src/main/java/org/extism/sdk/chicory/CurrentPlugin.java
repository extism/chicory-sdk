package org.extism.sdk.chicory;

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


    void setInput(byte[] input) {
        plugin.setInput(input);
    }

    byte[] getOutput() {
        return plugin.getOutput();
    }

    String getError() {
        return plugin.getError();
    }

}
