package io.github.sst.remake.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.io.GsonUtils;

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

    @Override
    public JsonObject asJson(JsonObject jsonObject) throws JsonParseException {
        JsonArray array = GsonUtils.getJSONArrayOrNull(jsonObject, this.name);
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                JsonObject settingObject = array.get(i).getAsJsonObject();
                String settingName = GsonUtils.getStringOrDefault(settingObject, "name", null);

                for (Setting<?> setting : this.subSettings) {
                    if (setting.name.equals(settingName)) {
                        setting.asJson(settingObject);
                        break;
                    }
                }
            }
        }

        this.value = GsonUtils.getBooleanOrDefault(jsonObject, "value", this.defaultValue);
        return jsonObject;
    }

    @Override
    public JsonObject fromJson(JsonObject jsonObject) {
        JsonArray children = new JsonArray();

        for (Setting<?> setting : this.subSettings) {
            children.add(setting.fromJson(new JsonObject()));
        }

        jsonObject.add("children", children);
        jsonObject.addProperty("name", this.name);
        return super.fromJson(jsonObject);
    }
}