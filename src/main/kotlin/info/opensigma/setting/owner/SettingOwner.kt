package info.opensigma.setting.owner

import info.opensigma.setting.Setting
import info.opensigma.system.ElementRepository
import info.opensigma.system.INameable

@Suppress("UNCHECKED_CAST", "unused")
class SettingOwner<T>(owner: INameable) : ElementRepository<Setting<T>>("${owner.name}-settings",
    Setting::class.java as Class<Setting<T>>, toScan = arrayOf(owner)) {

    fun getBooleanValue(name: String): Boolean {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Boolean
    }

    fun getIntValue(name: String): Int {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Int
    }

    fun getFloatValue(name: String): Float {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Float
    }

    fun getDoubleValue(name: String): Double {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Double
    }

    fun getLongValue(name: String): Long {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Long
    }

    fun getShortValue(name: String): Short {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Short
    }

    fun getByteValue(name: String): Byte {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Byte
    }

    fun getCharValue(name: String): Char {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as Char
    }

    fun getStringValue(name: String): String {
        return requireNotNull(getByName(name)?.value) { "Setting $name does not exist!" } as String
    }

    fun getObjectValue(name: String): Any {
        return requireNotNull(getByName(name)?.value as Any) { "Setting $name does not exist!" }
    }
}
