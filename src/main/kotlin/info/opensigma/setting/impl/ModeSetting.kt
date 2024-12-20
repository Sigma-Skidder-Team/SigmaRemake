package info.opensigma.setting.impl

import info.opensigma.setting.Setting
import org.apache.commons.lang3.ArrayUtils

class ModeSetting(
    override val name: String,
    description: String,
    val values: Array<String>,
    initialValue: String
) : Setting<String>(name, description) {

    override var value: String = initialValue
        set(value) {
            if (!ArrayUtils.contains(values, value)) return
            field = value
        }

    @Deprecated("Use the value field instead", replaceWith = ReplaceWith("value"))
    fun getValue(): String {
        return value
    }

    @Deprecated("Use the value field instead", replaceWith = ReplaceWith("value"))
    override fun setValue(value: String) {
        this.value = value
    }

    @Deprecated("Use the name field instead", replaceWith = ReplaceWith("name"))
    override fun getName(): String {
        return name
    }
}