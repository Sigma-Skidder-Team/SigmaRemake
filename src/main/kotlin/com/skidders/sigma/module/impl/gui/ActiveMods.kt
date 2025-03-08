package com.skidders.sigma.module.impl.gui

import com.google.common.eventbus.Subscribe
import com.skidders.SigmaReborn
import com.skidders.sigma.events.impl.Render2DEvent
import com.skidders.sigma.module.Category
import com.skidders.sigma.module.Module
import com.skidders.sigma.utils.render.font.styled.StyledFont
import com.skidders.sigma.module.settings.impl.BooleanSetting
import com.skidders.sigma.module.settings.impl.ModeSetting
import com.skidders.sigma.module.settings.impl.NumberSetting
import com.skidders.sigma.utils.mc
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer
import com.skidders.sigma.utils.render.interfaces.IFontRegistry
import org.lwjgl.glfw.GLFW

import java.awt.*

class ActiveMods : Module {
    var font: StyledFont = IFontRegistry.Light20

    constructor() : super("ActiveMods", "Renders active mods", Category.GUI, GLFW.GLFW_KEY_V) {
        registerSetting(
            ModeSetting(
                "Size",
                "The font size",
                "Normal",
                arrayOf("Normal", "Small", "Tiny")
            )
        )
        registerSetting(BooleanSetting("Animations", "Scale in animation", true))
        registerSetting(
            NumberSetting(
                "Slider", "OMG, it's a slider!",
                3, 1f,
                5f, 1.0f
            )
        )
    }

    @Override
    override fun onEnable() {
        when (getSettingByName("Size").value as String) {
            "Normal" -> font = IFontRegistry.Light20
            "Small" -> font = IFontRegistry.Light18
            "Tiny" -> font = IFontRegistry.Light14
        }
    }

    @Subscribe
    fun on2D(event: Render2DEvent) {
        if (mc.options.debugEnabled) {
            return
        }

        var offsetY = 3
        val screenWidth = mc.window.width
        for (module in SigmaReborn.INSTANCE.moduleManager.modules) {
            val y: Float = offsetY.toFloat()

            val x: Float = (screenWidth.toFloat()) / 2 - font.getWidth(module.name) - 3

            StyledFontRenderer.drawString(
                event.matrixStack,
                font,
                module.name,
                x.toDouble(),
                y.toDouble(),
                Color(255, 255, 255, 150)
            )

            offsetY += 12
        }
    }

}
