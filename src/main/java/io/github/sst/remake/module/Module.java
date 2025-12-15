package io.github.sst.remake.module;

import io.github.sst.remake.Client;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.util.IMinecraft;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class Module implements IMinecraft {

    public final List<Setting<?>> settings = new ArrayList<>();
    public final String name, description;
    public final Category category;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;

        Client.INSTANCE.moduleManager.currentModule = this;
    }

    public Module(Category category, String name, String description) {
        this(name, description, category);
    }

    public boolean enabled;
    public int keycode = 0;

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onInit() {
    }

    public void toggle() {
        setEnabled(!enabled);

        if (this.enabled) {
            Client.BUS.register(this);
            this.onEnable();
        } else {
            Client.BUS.unregister(this);
            this.onDisable();
        }
    }

}
