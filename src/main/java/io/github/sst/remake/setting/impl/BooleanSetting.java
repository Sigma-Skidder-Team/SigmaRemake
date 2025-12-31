package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.io.GsonUtils;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, Boolean value) {
        super(name, description, SettingType.CHECKBOX,  value);
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        this.value = GsonUtils.getBooleanOrDefault(jsonObject, "value", this.defaultValue);
        return jsonObject;
    }
}


