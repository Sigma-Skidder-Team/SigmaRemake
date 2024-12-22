package info.opensigma.setting.impl

import info.opensigma.setting.Setting
import org.apache.commons.lang3.ArrayUtils

class ModeSetting(
    override val name: String,
    description: String,
    val values: Array<String>,
    initialValue: String
) : Setting<String>(name) {

    override var value: String = initialValue
        set(value) {
            if (!ArrayUtils.contains(values, value)) return
            field = value
        }
}