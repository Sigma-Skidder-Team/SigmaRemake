package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

public class TextInputSetting extends Setting<String> {
    public TextInputSetting(String name, String description, String value) {
        super(name, description, SettingType.TEXT_INPUT, value);
    }
}
