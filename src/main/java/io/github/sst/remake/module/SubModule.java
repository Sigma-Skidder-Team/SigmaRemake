package io.github.sst.remake.module;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;

public class SubModule implements IMinecraft {
    public final String name;
    public Module parent;
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

    public boolean isEnabled() {
        return enabled;
    }
}
