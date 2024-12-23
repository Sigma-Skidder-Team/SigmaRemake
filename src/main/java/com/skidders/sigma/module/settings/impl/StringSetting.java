package com.skidders.sigma.module.settings.impl;

import com.skidders.sigma.module.settings.Setting;

public class StringSetting extends Setting<String> {

    public String[] allValues;

    public StringSetting(String name, String desc, String value, String[] allValues) {
        super(name, desc, value);
        this.allValues = allValues;
    }
}
