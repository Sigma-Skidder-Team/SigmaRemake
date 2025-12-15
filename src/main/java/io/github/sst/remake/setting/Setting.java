package io.github.sst.remake.setting;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.client.SettingChangeListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
public class Setting<T> {
    private final List<SettingChangeListener<T>> listeners = new ArrayList<>();
    private BooleanSupplier hidden = () -> false;

    public final String name, description;
    private T value;

    public Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;

        Client.INSTANCE.moduleManager.currentModule.settings.add(this);
    }

    public void setValue(T value) {
        T oldValue = this.value;
        this.value = value;

        for (SettingChangeListener<T> listener : listeners) {
            listener.onSettingChanged(this, oldValue, value);
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

    public void addListener(SettingChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingChangeListener<T> listener) {
        listeners.remove(listener);
    }
}
