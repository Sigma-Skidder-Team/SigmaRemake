package com.skidders.sigma.module.settings.impl;

import com.skidders.sigma.module.settings.Setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String desc, boolean value) {
        super(name, desc, value);
    }
}
