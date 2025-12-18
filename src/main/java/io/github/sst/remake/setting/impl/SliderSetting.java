package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

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
}
