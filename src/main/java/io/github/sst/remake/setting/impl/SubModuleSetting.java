package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.data.setting.DropdownSetting;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.system.io.GsonUtils;

import java.util.Arrays;
import java.util.List;

public class SubModuleSetting extends Setting<SubModule> implements DropdownSetting {
    public final List<SubModule> modes;

    public SubModuleSetting(String name, String description, int index, SubModule... modes) {
        super(name, description, SettingType.DROPDOWN, modes[index]);
        this.modes = Arrays.asList(modes);
    }

    public SubModuleSetting(String name, String description, SubModule... modes) {
        this(name, description, 0, modes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getModeLabels() {
        List<String> labels = new java.util.ArrayList<>(modes.size());
        for (SubModule m : modes) {
            labels.add(m != null && m.name != null ? m.name : "null");
        }
        return labels;
    }

    public int getModeIndex() {
        int index = 0;
        for (SubModule mode : this.modes) {
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
        String savedName = GsonUtils.getStringOrDefault(jsonObject, "value", this.defaultValue.name);

        SubModule resolved = this.defaultValue;
        for (SubModule mode : this.modes) {
            if (mode != null && mode.name != null && mode.name.equals(savedName)) {
                resolved = mode;
                break;
            }
        }

        this.setValue(resolved);
        return jsonObject;
    }
}