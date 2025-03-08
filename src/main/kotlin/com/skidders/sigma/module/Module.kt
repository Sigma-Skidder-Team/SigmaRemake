package com.skidders.sigma.module

import com.skidders.SigmaReborn
import com.skidders.sigma.module.settings.Setting
import com.skidders.sigma.utils.IMinecraft

import java.util.ArrayList

class Module : IMinecraft {

    lateinit var name: String
    lateinit var desc: String
    val category: Category
    var enabled: Boolean
    var key: Int? = null

    constructor(name: String, desc: String, category: Category) {
        this.name = name
        this.desc = desc
        this.category = category
        this.enabled = false
    }

    constructor(name: String, desc: String, category: Category, key: Int) : this(name, desc, category) {
        this.key = key
    }

    fun onEnable() {}
    fun onDisable() {}

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled

        if (enabled) {
            SigmaReborn.EVENT_BUS.register(this)
            onEnable()
        } else {
            onDisable()
            SigmaReborn.EVENT_BUS.unregister(this)
        }
    }

    val settings: ArrayList<Setting<*>> = ArrayList<Setting<*>>()

    fun registerSetting(setting: Setting<*>) {
        if (!settings.contains(setting)) {
            settings.add(setting)
        } else {
            throw IllegalArgumentException("Attempted to add an duplicate setting.")
        }
    }

    fun getSettingByName(input: String): Setting<*> {
        return settings.stream().filter { it.name.equals(input, ignoreCase = true) }.findFirst()
            .orElse(null)
    }

}
