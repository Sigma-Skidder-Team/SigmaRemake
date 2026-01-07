package io.github.sst.remake.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.io.GsonUtils;

public class CurveSetting extends Setting<CurveSetting.Curve> {

    public CurveSetting(String name, String description, float initial, float mid, float finalStage, float maximum) {
        super(name, description, SettingType.CURVE, new Curve(initial, mid, finalStage, maximum));
    }

    public void setValue(float initial, float mid, float finalStage, float maximum) {
        setValue(initial, mid, finalStage, maximum, true);
    }

    public float[] getValue() {
        Curve profile = value;
        return new float[]{profile.initial, profile.mid, profile.finalStage, profile.maximum};
    }

    public void setValue(float initial, float mid, float finalStage, float maximum, boolean notify) {
        Curve newValue = new Curve(initial, mid, finalStage, maximum);
        if (!value.equals(newValue)) {
            value = newValue;
            if (notify) {
                notifyListeners();
            }
        }
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) throws JsonParseException {
        this.value = new Curve(GsonUtils.getJSONArrayOrNull(jsonObject, "value"));
        return jsonObject;
    }

    @Override
    public void loadFromJson(com.google.gson.JsonElement element) {
        if (element.isJsonArray()) {
            this.value = new Curve(element.getAsJsonArray());
        }
    }

    @Override
    public JsonObject fromJson(JsonObject jsonObject) {
        jsonObject.addProperty("name", name);

        Curve profile = value;

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(profile.initial);
        jsonArray.add(profile.mid);
        jsonArray.add(profile.finalStage);
        jsonArray.add(profile.maximum);

        jsonObject.add("value", jsonArray);
        return jsonObject;
    }

    public static class Curve {
        public float initial;
        public float mid;
        public float finalStage;
        public float maximum;

        public Curve(float initial, float mid, float finalStage, float maximum) {
            this.initial = initial;
            this.mid = mid;
            this.finalStage = finalStage;
            this.maximum = maximum;
        }

        public Curve(JsonArray jsonArray) throws JsonParseException {
            this.initial = Float.parseFloat(jsonArray.get(0).getAsString());
            this.mid = Float.parseFloat(jsonArray.get(1).getAsString());
            this.finalStage = Float.parseFloat(jsonArray.get(2).getAsString());
            this.maximum = Float.parseFloat(jsonArray.get(3).getAsString());
        }
    }
}
