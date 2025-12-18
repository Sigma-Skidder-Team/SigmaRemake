package io.github.sst.remake.setting.impl;

import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;

import java.awt.*;

public class ColorSetting extends Setting<Integer> {
    public boolean rainbow;

    public ColorSetting(String name, String description, int value, boolean rainbow) {
        super(name, description, SettingType.COLOR,  value);
        this.rainbow = rainbow;
    }

    public ColorSetting(String name, String description, int value) {
        this(name, description, value, false);
    }

    public Integer getValue() {
        if (!this.rainbow) {
            return value;
        } else {
            Color color = new Color(value);
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            return Color.getHSBColor((float) (System.currentTimeMillis() % 4000L) / 4000.0F, hsb[1], hsb[2]).getRGB();
        }
    }
}
