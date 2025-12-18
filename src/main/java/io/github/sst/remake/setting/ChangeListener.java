package io.github.sst.remake.setting;

@FunctionalInterface
public interface ChangeListener<T> {
    void onSettingChanged(Setting<T> setting, T oldValue, T newValue);
}