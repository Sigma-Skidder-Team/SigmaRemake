package com.skidders.sigma.screens.clickgui

import com.skidders.SigmaReborn
import com.skidders.sigma.module.Module
import com.skidders.sigma.module.settings.impl.BooleanSetting
import com.skidders.sigma.module.settings.impl.NumberSetting
import com.skidders.sigma.utils.misc.MouseHandler
import com.skidders.sigma.utils.render.RenderUtil
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.glfw.GLFW
import java.awt.*

class SettingGUI {
    private val parent: Module
    private val screen: ClickGUI
    private val mouseHandler: MouseHandler

    constructor(parent: Module) {
        this.parent = parent
        this.screen = SigmaReborn.INSTANCE.screenProcessor.clickGUI
        this.mouseHandler = MouseHandler(MinecraftClient.getInstance().window.handle)
    }

    fun draw(matrices: MatrixStack, mouseX: Double, mouseY: Double) {
        val width = 210
        val height = 240
        val x = screen.width.toFloat() / 2 - width / 2
        val y = screen.height.toFloat() / 2 - height / 2

        RenderUtil.drawRectangle(matrices, 0F, 0F, screen.width.toFloat(), screen.height.toFloat(), Color(0, 0, 0, 150))

        RenderUtil.drawRectangle(matrices, x, y, width.toFloat(), height.toFloat(), Color(254, 254, 254))
        StyledFontRenderer.drawString(matrices, screen.light20, parent.name, x.toDouble(),
            (y - 30).toDouble(), Color(254, 254, 254))
        StyledFontRenderer.drawString(matrices, screen.light20, parent.desc,
            (x + 12).toDouble(), (y + 15).toDouble(), Color(100, 100, 100))

        val mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        if (!parent.settings.isEmpty()) {
            var offset = y + 35
            for (setting in parent.settings) {
                //full setting bounds - x, offset - 2, width, 18
                StyledFontRenderer.drawString(matrices, screen.settingName, setting.name, (x + 12).toDouble(),
                    offset.toDouble(), Color.BLACK)

                if (RenderUtil.hovered(mouseX, mouseY, x, offset - 2, width.toFloat(), 18F)) {
                    StyledFontRenderer.drawString(
                        matrices,
                        screen.settingSB,
                        "§l" + setting.name,
                        (x + 7).toDouble(),
                        (y + height + 7).toDouble(),
                        Color(255, 255, 255, 127)
                    )
                    StyledFontRenderer.drawString(
                        matrices,
                        screen.settingS,
                        setting.desc,
                        (x + 7 + screen.settingSB.getWidth(setting.name)).toDouble(),
                        (y + height + 7).toDouble(),
                        Color(255, 255, 255, 127)
                    )
                }

                if (setting is BooleanSetting) {
                    setting.value = setting.checkboxComponent.draw(mouseHandler, setting.value, mouseX, mouseY, x + width - 20, offset + 1.3f)
                } else if (setting is NumberSetting) {
                    val min = setting.min
                    val max = setting.max
                    val value = setting.value.toFloat()
                    setting.getDecimalPlaces()

                    val normalizedValue = (value - min) / (max - min)
                    val sliderWidth = normalizedValue * 50

                    val bgColor = Color(215, 234, 254)
                    val filledColor = Color(59, 153, 253)

                    RenderUtil.drawRectangle(matrices, x + width - 66, offset + 5.5f, 50F, 3.5f, bgColor)
                    RenderUtil.drawRectangle(matrices, x + width - 66, offset + 5.5f, sliderWidth, 3.5f, filledColor)

                    val circleX = x + width - 66 + sliderWidth

                    val hoverSlider = RenderUtil.hovered(mouseX, mouseY, x + width - 69, offset + 2, 55F, 10F)
                    if (hoverSlider) {
                        val textOffset: Int = if (value <= min + (max - min) * 0.07) -5 else 0

                        StyledFontRenderer.drawString(
                            matrices,
                            screen.sliderValue,
                            setting.value.toString(),
                            (x + width - 70 - screen.sliderValue.getWidth(setting.value.toString()) + textOffset).toDouble(),
                            (offset + 4.25),
                            Color(125, 125, 125)
                        )
                        if (mouseDown) {
                            val normalizedX = (mouseX - (x + width - 66)) / (50)
                            var newValue: Float = (min + normalizedX * (max - min)).toFloat()
                            newValue = newValue.coerceAtLeast(min).coerceAtMost(max)
                            setting.value = newValue
                        }
                    }

                    RenderUtil.drawCircle(circleX, offset + 7, 6.0, Color(200, 200, 200))
                    RenderUtil.drawCircle(circleX, offset + 7, 5.5, Color(254, 254, 254))
                }

                offset += 18
            }
        }
    }

    fun click(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val width = 210
        val height = 240
        val x = screen.width.toFloat() / 2 - width / 2
        val y = screen.height.toFloat() / 2 - height / 2

        if (button == 0) {
            if (!RenderUtil.hovered(mouseX, mouseY, x, y, width.toFloat(), height.toFloat())) {
                screen.settingGUI = null
            }
        }

        return true
    }


}
