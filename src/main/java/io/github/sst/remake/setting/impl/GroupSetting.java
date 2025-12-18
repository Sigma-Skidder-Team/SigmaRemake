package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

import java.util.Arrays;
import java.util.List;

public class GroupSetting extends Setting<Boolean> {
    public final List<Setting<?>> subSettings;

    public GroupSetting(String name, String description, boolean value, List<Setting<?>> subSettings) {
        super(name, description, SettingType.GROUP, value);
        this.subSettings = subSettings;
    }

    public GroupSetting(String name, String description, boolean value, Setting<?>... subSettings) {
        this(name, description, value, Arrays.asList(subSettings));
    }
}