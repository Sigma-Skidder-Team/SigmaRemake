package io.github.sst.remake.setting.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.util.system.io.GsonUtils;

public class SliderSetting extends Setting<Float> {
    public float min, max, increment;

    public SliderSetting(String name, String description, float value, float min, float max, float increment) {
        super(name, description, SettingType.SLIDER, value);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public int getPlaces() {
        if (this.increment != 1.0F) {
            String stepString = Float.toString(Math.abs(this.increment));
            int decimalPointIndex = stepString.indexOf('.');
            return stepString.length() - decimalPointIndex - 1;
        } else {
            return 0;
        }
    }

    @Override
    public JsonObject asJson(JsonObject jsonObject) {
        this.value = GsonUtils.getFloatOrDefault(jsonObject, "value", this.defaultValue);
        return jsonObject;
    }

}
