package com.skidders.sigma.managers

import com.google.common.eventbus.Subscribe
import com.skidders.sigma.events.impl.KeyPressEvent
import com.skidders.sigma.module.Category
import com.skidders.sigma.module.Module
import com.skidders.sigma.module.impl.gui.ActiveMods
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

import java.util.stream.Collectors

class ModuleManager {

    val modules: ArrayList<Module> = arrayListOf()

    constructor() {
        modules.add(ActiveMods())
    }

    @Suppress("UNUSED")
    @Subscribe
    fun onKey(event: KeyPressEvent) {
        if (event.action == GLFW.GLFW_RELEASE && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().currentScreen == null)
            this.modules.forEach {
                if (it.key == event.key) {
                    it.setEnabled(!it.enabled)
                }
            }
    }

    fun getModuleByName(input: String): Module {
        return modules.stream().filter { it.name.equals(input, ignoreCase = true) }.findFirst().orElse(null)
    }

    fun getEnabledModules(): List<Module> {
        return modules.stream().filter { it.enabled }.collect(Collectors.toList())
    }

    fun <V : Module> getModuleByClass(clazz: Class<V>): V? {
        val mod = modules.stream().filter { it.javaClass.equals(clazz) }.findFirst().orElse(null)

        if (mod == null) {
            return null
        }

        return clazz.cast(mod)
    }

    fun getModulesByCategory(input: Category): List<Module> {
        return modules.stream().filter { it.category == input }.collect(Collectors.toList()
        )
    }

}
