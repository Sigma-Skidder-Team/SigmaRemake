package io.github.sst.remake.module;

import io.github.sst.remake.Client;
import io.github.sst.remake.module.impl.gui.ActiveModsModule;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.system.io.audio.SoundUtils;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module implements IMinecraft {

    public final List<Setting<?>> settings = new ArrayList<>();
    public final String name, description;
    public final Category category;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
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
        findSettings();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        if (this.enabled) {
            Client.BUS.register(this);
            if (!Client.INSTANCE.moduleManager.isToggleSoundSuppressed()
                    && Client.INSTANCE.moduleManager.getModule(ActiveModsModule.class).toggleSound.value) {
                SoundUtils.play("activate");
            }
            this.onEnable();
        } else {
            Client.BUS.unregister(this);
            if (!Client.INSTANCE.moduleManager.isToggleSoundSuppressed()
                    && Client.INSTANCE.moduleManager.getModule(ActiveModsModule.class).toggleSound.value) {
                SoundUtils.play("deactivate");
            }
            this.onDisable();
        }

        toggleSubModules(enabled);
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    @SuppressWarnings("rawtypes")
    private void findSettings() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!Setting.class.isAssignableFrom(field.getType())) continue;
            field.setAccessible(true);

            try {
                Setting setting = (Setting) field.get(this);
                this.settings.add(setting);

                if (setting instanceof SubModuleSetting ) {
                    SubModuleSetting sms = (SubModuleSetting) setting;
                    for (SubModule mode : sms.modes) {
                        mode.parent = this;
                        findSubModuleSettings(sms, mode);
                    }
                }
            } catch (IllegalAccessException e) {
                Client.LOGGER.error("Failed to access setting field {}", field.getName(), e);
            }
        }
    }

    private void findSubModuleSettings(SubModuleSetting sms, SubModule mode) {
        for (Field field2 : mode.getClass().getDeclaredFields()) {
            if (!Setting.class.isAssignableFrom(field2.getType())) continue;
            field2.setAccessible(true);

            try {
                Setting inner = (Setting) field2.get(mode);

                final SubModule owner = mode;

                inner.hide(() -> sms.value != owner);
                this.settings.add(inner);
            } catch (IllegalAccessException e) {
                Client.LOGGER.error("Failed to access submodule inner setting field {}", field2.getName(), e);
            }
        }
    }

    private void toggleSubModules(boolean enabled) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!SubModuleSetting.class.isAssignableFrom(field.getType())) continue;
            field.setAccessible(true);

            try {
                SubModuleSetting setting = (SubModuleSetting) field.get(this);
                setting.value.setEnabled(enabled);
            } catch (IllegalAccessException e) {
                Client.LOGGER.error("Failed to access submodule setting field {}", field.getName(), e);
            }
        }
    }
}