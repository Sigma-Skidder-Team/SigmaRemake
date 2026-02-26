package io.github.sst.remake.module;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import lombok.Getter;

public class SubModule implements IMinecraft {
    public final String name;
    @Getter
    public Module parent;
    @Getter
    private boolean enabled;

    public SubModule(String name) {
        this.name = name;
    }

    public void onEnable() {}
    public void onDisable() {}

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        if (enabled) {
            onEnable();
            Client.BUS.register(this);
        } else {
            Client.BUS.unregister(this);
            onDisable();
        }
    }
}
