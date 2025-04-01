package com.skidders.sigma.module;

import com.skidders.sigma.module.settings.Setting;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.client.interfaces.ISubscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Module implements IMinecraft, ISubscriber {

    public final String name,desc;
    public final Category category;
    public boolean enabled;
    public int key;

    public Module(String name, String desc, Category category, int key) {
        this(name, desc, category);
        this.key = key;
    }

    public void onEnable() {}
    public void onDisable() {}

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            bus.register(this);
            onEnable();
        } else {
            onDisable();
            bus.unregister(this);
        }
    }

    public List<Setting<?>> settings = new ArrayList<>();

    public void registerSetting(Setting<?> setting) {
        if (!settings.contains(setting)) {
            settings.add(setting);
        } else {
            throw new IllegalArgumentException("Attempted to add an duplicate setting.");
        }
    }

    public Setting<?> getSettingByName(String input) {
        return settings.stream().filter(s -> s.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

}
