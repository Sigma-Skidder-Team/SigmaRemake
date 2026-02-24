package io.github.sst.remake.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.system.io.GsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockListSetting extends Setting<List<String>> {
    public boolean enabled;

    public BlockListSetting(String name, String description, boolean enabled, String... values) {
        super(name, description, SettingType.BLOCKS, Arrays.asList(values));
        this.enabled = enabled;
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        JsonArray jsonArray = GsonUtils.getJSONArrayOrNull(jsonObject, "value");
        this.value = new ArrayList<>();

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                this.value.add(jsonArray.get(i).getAsString());
            }
        }

        return jsonObject;
    }

    @Override
    public JsonObject fromJson(JsonObject jsonObject) {
        jsonObject.addProperty("name", this.name);

        JsonArray jsonArray = new JsonArray();
        for (Object value : this.value) {
            jsonArray.add(value.toString());
        }

        jsonObject.add("value", jsonArray);

        return jsonObject;
    }

    @Override
    public void loadFromJson(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            List<String> list = new ArrayList<>();
            for (JsonElement e : array) {
                list.add(e.getAsString());
            }
            this.value = list;
        } else {
            this.value = new ArrayList<>();
        }
    }

}
