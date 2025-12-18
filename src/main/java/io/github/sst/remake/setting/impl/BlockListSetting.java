package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

import java.util.Arrays;
import java.util.List;

public class BlockListSetting extends Setting<List<String>> {
    public boolean enabled;

    public BlockListSetting(String name, String description, boolean enabled, String... values) {
        super(name, description, SettingType.BLOCKS, Arrays.asList(values));
        this.enabled = enabled;
    }
}
