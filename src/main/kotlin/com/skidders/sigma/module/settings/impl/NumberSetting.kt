package com.skidders.sigma.module.settings.impl

import com.skidders.sigma.module.settings.Setting
import kotlin.math.abs

class NumberSetting : Setting<Number> {

    val min: Float
    val max: Float
    val step: Float

    constructor(name: String, desc: String, value: Number, min: Float, max: Float, step: Float) : super(name, desc, value) {
        this.min = min
        this.max = max
        this.step = step
    }

    fun getDecimalPlaces(): Int {
        if (this.step != 1.0F) {
            val stepString = "${abs(this.step)}"
            val decimalPointIndex = stepString.indexOf('.')
            return stepString.length - decimalPointIndex - 1
        } else {
            return 0
        }
    }
}
