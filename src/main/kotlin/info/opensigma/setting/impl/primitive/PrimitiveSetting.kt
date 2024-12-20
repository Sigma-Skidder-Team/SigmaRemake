package info.opensigma.setting.impl.primitive

import info.opensigma.setting.Setting
import java.util.function.Predicate

/**
 * A simple setting that just sets one and gets one single value without any additional information.
 *
 * @param T The type of the setting.
 */
open class PrimitiveSetting<T>(
    name: String,
    description: String,
    override var value: T,
    var type: PrimitiveSettingType? = null,
    private val verifier: Predicate<T>? = null
) : Setting<T>(name, description) {

    init {
        if (type == null && value != null) {
            this.type = when (value!!::class) {
                Boolean::class -> PrimitiveSettingType.BOOLEAN
                Int::class -> PrimitiveSettingType.COLOR
                else -> null
            }
        }
    }

    constructor(name: String, description: String, value: T, type: PrimitiveSettingType) :
        this(name, description, value, type, null)

    constructor(name: String, description: String, value: T) :
        this(name, description, value, null, null)

    fun getValue(): T {
        return value
    }

    override fun setValue(value: T) {
        if (verifier?.test(value) == true) {
            this.value = value
        }
    }

    @Deprecated("Use the name field instead", replaceWith = ReplaceWith("name"))
    override fun getName(): String {
        return name
    }
}