package com.skidders.sigma.module;

import com.skidders.sigma.module.settings.Setting;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.client.interfaces.ISubscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class Module implements IMinecraft, ISubscriber {

    private final String name, desc;
    private final Category category;
    private boolean enabled;
    private int key;

    public Module(String name, String desc, Category category, int key) {
        this(name, desc, category);
        this.key = key;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onInit() {}

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
