package com.skidders.sigma.module.settings.impl;

import com.skidders.sigma.module.settings.Setting;
import com.skidders.sigma.screens.clickgui.components.CheckboxComponent;

public class BooleanSetting extends Setting<Boolean> {
    public CheckboxComponent checkboxComponent;
    public BooleanSetting(String name, String desc, boolean value) {
        super(name, desc, value);
        this.checkboxComponent = new CheckboxComponent();
    }
}
