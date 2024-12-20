package info.opensigma.setting.owner

import info.opensigma.setting.Setting
import info.opensigma.system.ElementRepository
import info.opensigma.system.INameable
import net.jezevcik.argon.utils.objects.NullUtils

class SettingOwner(owner: INameable) : ElementRepository<Setting>(
    "${owner.name}-settings", Setting::class.java as Class<T>, arrayOf(owner)
) {

    fun getBooleanValue(name: String): Boolean {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Boolean
    }

    fun getIntValue(name: String): Int {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Int
    }

    fun getFloatValue(name: String): Float {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Float
    }

    fun getDoubleValue(name: String): Double {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Double
    }

    fun getLongValue(name: String): Long {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Long
    }

    fun getShortValue(name: String): Short {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Short
    }

    fun getByteValue(name: String): Byte {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Byte
    }

    fun getCharValue(name: String): Char {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as Char
    }

    fun getStringValue(name: String): String {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!") as String
    }

    fun getObjectValue(name: String): Any {
        return NullUtils.requireNotNull(getByName(name)?.getValue(), "Setting $name does not exist!")
    }
}