package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.system.io.GsonUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class ModeSetting extends Setting<String> {
    public final List<String> modes;

    public ModeSetting(String name, String description, String value, String... modes) {
        super(name, description, SettingType.DROPDOWN, value);
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String description, int value, String... modes) {
        this(name, description, modes[value], modes);
    }

    public int getModeIndex() {
        int index = 0;

        for (String mode : this.modes) {
            if (mode.equals(this.value)) {
                return index;
            }

            index++;
        }

        return 0;
    }

    public void setModeByIndex(int index) {
        if (index < this.modes.size()) {
            String mode = this.modes.get(index);
            this.setValue(mode);
        }
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        this.value = GsonUtils.getStringOrDefault(jsonObject, "value", this.defaultValue);
        boolean isValid = this.modes.contains(this.value);

        if (!isValid) {
            this.value = this.defaultValue;
        }

        return jsonObject;
    }
}
