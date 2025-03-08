package com.skidders.sigma.module.settings.impl

import com.skidders.sigma.module.settings.Setting

class ModeSetting : Setting<String> {

    val allValues: Array<String>

    constructor(name: String, desc: String, value: String, allValues: Array<String>) : super(name, desc, value) {
        this.allValues = allValues
    }
}
