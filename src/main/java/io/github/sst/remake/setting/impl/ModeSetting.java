package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.data.setting.DropdownSetting;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.system.io.GsonUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class ModeSetting extends Setting<String> implements DropdownSetting {
    public final List<String> modes;

    public ModeSetting(String name, String description, String value, String... modes) {
        super(name, description, SettingType.DROPDOWN, value);
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String description, int value, String... modes) {
        this(name, description, modes[value], modes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getModeLabels() {
        return modes;
    }

    public int getModeIndex() {
        int index = 0;
        for (String mode : this.modes) {
            if (mode.equals(this.value)) return index;
            index++;
        }
        return 0;
    }

    public void setModeByIndex(int index) {
        if (index < this.modes.size()) this.setValue(this.modes.get(index));
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        this.value = GsonUtils.getStringOrDefault(jsonObject, "value", this.defaultValue);
        if (!this.modes.contains(this.value)) this.value = this.defaultValue;
        return jsonObject;
    }
}