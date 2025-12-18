package io.github.sst.remake.setting;

import io.github.sst.remake.Client;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Setting<T> {
    private final List<ChangeListener<T>> listeners = new ArrayList<>();
    private BooleanSupplier hidden = () -> false;

    public final String name, description;
    public final SettingType settingType;
    public final T defaultValue;
    public T value;

    public Setting(String name, String description, SettingType type, T value) {
        this.name = name;
        this.description = description;
        this.settingType = type;
        this.value = value;
        this.defaultValue = value;

        Client.INSTANCE.moduleManager.currentModule.settings.add(this);
    }

    public void setValue(T value) {
        setValue(value, true);
    }

    public void setValue(T value, boolean notify) {
        this.value = value;

        if (notify)
            for (ChangeListener<T> listener : listeners) {
                listener.onSettingChanged(this);
            }
    }

    @SuppressWarnings("unchecked")
    public <I extends Setting<?>> I hide(BooleanSupplier hidden) {
        this.hidden = hidden;
        return (I) this;
    }

    public boolean isHidden() {
        return hidden.getAsBoolean();
    }

    public void addListener(ChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ChangeListener<T> listener) {
        listeners.remove(listener);
    }
}
