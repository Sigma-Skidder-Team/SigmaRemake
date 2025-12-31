package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.io.GsonUtils;

public class TextInputSetting extends Setting<String> {
    public TextInputSetting(String name, String description, String value) {
        super(name, description, SettingType.TEXT_INPUT, value);
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        this.value = GsonUtils.getStringOrDefault(jsonObject, "value", this.defaultValue);
        return jsonObject;
    }
}
