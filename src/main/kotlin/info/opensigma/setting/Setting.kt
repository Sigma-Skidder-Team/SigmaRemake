package info.opensigma.setting

import info.opensigma.system.INameable

abstract class Setting<T>(
    override val name: String,
    val description: String
) : INameable {

    abstract val value: T
    abstract fun setValue(value: T)
}