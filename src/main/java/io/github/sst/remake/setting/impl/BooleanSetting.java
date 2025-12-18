package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, Boolean value) {
        super(name, description, SettingType.CHECKBOX,  value);
    }
}
