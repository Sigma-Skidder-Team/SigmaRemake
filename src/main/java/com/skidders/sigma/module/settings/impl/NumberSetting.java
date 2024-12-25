package com.skidders.sigma.module.settings.impl;

import com.skidders.sigma.module.settings.Setting;

public class NumberSetting extends Setting<Number> {

    public final float min, max, step;

    public NumberSetting(String name, String desc, Number value, float min, float max, float step) {
        super(name, desc, value);

        this.min = min;
        this.max = max;
        this.step = step;
    }

    public int getDecimalPlaces() {
        if (this.step != 1.0F) {
            String stepString = Float.toString(Math.abs(this.step));
            int decimalPointIndex = stepString.indexOf('.');
            return stepString.length() - decimalPointIndex - 1;
        } else {
            return 0;
        }
    }
}
