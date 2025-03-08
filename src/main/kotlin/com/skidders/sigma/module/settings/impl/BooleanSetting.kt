package com.skidders.sigma.module.settings.impl

import com.skidders.sigma.module.settings.Setting
import com.skidders.sigma.screens.clickgui.components.CheckboxComponent

class BooleanSetting : Setting<Boolean> {
    val checkboxComponent: CheckboxComponent = CheckboxComponent()
    constructor(name: String, desc: String, value: Boolean) : super(name, desc, value)
}
