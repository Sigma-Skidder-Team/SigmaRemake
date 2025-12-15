package io.github.sst.remake.util.client;

import io.github.sst.remake.setting.Setting;

@FunctionalInterface
public interface SettingChangeListener<T> {
    void onSettingChanged(Setting<T> setting, T oldValue, T newValue);
}