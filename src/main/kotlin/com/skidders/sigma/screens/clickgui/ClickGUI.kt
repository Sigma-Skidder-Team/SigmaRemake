package com.skidders.sigma.screens.clickgui

import com.mojang.blaze3d.systems.RenderSystem
import com.skidders.SigmaReborn
import com.skidders.sigma.module.Category
import com.skidders.sigma.module.Module
import com.skidders.sigma.utils.IMinecraft
import com.skidders.sigma.utils.IMinecraft.mc
import com.skidders.sigma.utils.render.RenderUtil
import com.skidders.sigma.utils.render.font.styled.StyledFont
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer
import com.skidders.sigma.utils.render.interfaces.IFontRegistry
import com.skidders.sigma.utils.render.shader.ShaderRenderUtil
import com.skidders.sigma.utils.render.shader.shader.impl.BlurShader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.awt.Point

class ClickGUI(title: String) : Screen(Text.of(title)), IMinecraft {

    val moduleName: StyledFont = IFontRegistry.Medium40
    val settingName: StyledFont = IFontRegistry.Light24
    val sliderValue: StyledFont = IFontRegistry.Light14
    val stringValue: StyledFont = IFontRegistry.Light18
    val settingS: StyledFont = IFontRegistry.SRegular17
    val settingSB: StyledFont = IFontRegistry.SBold17

    private val light25: StyledFont = IFontRegistry.Light25
    val light20: StyledFont = IFontRegistry.Light20

    private val categoryPositions: MutableMap<Category, Point> = HashMap()
    private val moduleHeight = 14

    private var draggingCategory: Category? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    private val frameWidth = 110f
    private val frameHeight = 120f
    private val categoryHeight = 27f

    private var hoveredModule: Module? = null

    var settingGUI: SettingGUI? = null

    init {
        var xOffsetStart = 7f
        var yOffsetStart = 10f
        var xOffset = xOffsetStart
        var yOffset = yOffsetStart
        var count = 0
        val columns = 4

        for (category in Category.entries) {
            categoryPositions[category] = Point(xOffset.toInt(), yOffset.toInt())
            xOffset += frameWidth + 5

            if (++count % columns == 0) {
                xOffset = xOffsetStart
                yOffset += categoryHeight + frameWidth + yOffsetStart + 5
            }
        }
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != -1) {
            if (button == 0) {
                for ((category, position) in categoryPositions) {
                    if (mouseX >= position.x && mouseX <= position.x + frameWidth &&
                        mouseY >= position.y && mouseY <= position.y + categoryHeight
                    ) {
                        draggingCategory = category
                        dragOffsetX = (mouseX - position.x).toInt()
                        dragOffsetY = (mouseY - position.y).toInt()

                        return true
                    }
                }
            }

            for (category in Category.values()) {
                val position = categoryPositions[category] ?: continue

                var xOffset = position.x
                var yOffset = position.y

                var modOffset = yOffset + categoryHeight
                for (module in SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(category)) {
                    if (RenderUtil.hovered(mouseX, mouseY, xOffset.toFloat(), modOffset, frameWidth,
                            moduleHeight.toFloat()
                        )) {
                        if (button == 0) {
                            hoveredModule = module
                        } else if (button == 1) {
                            settingGUI = SettingGUI(module)
                        }
                    }

                    modOffset += moduleHeight
                }
            }
        }

        settingGUI?.click(mouseX, mouseY, button)

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            if (draggingCategory != null) {
                draggingCategory = null
                return true
            }

            if (hoveredModule != null) {
                val position = categoryPositions[Category.valueOf(hoveredModule!!.category.name)]
                var xOffset = position!!.x
                var yOffset = position.y

                var modOffset =
                    yOffset + categoryHeight + SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(hoveredModule!!.category)
                        .indexOf(hoveredModule) * moduleHeight
                if (RenderUtil.hovered(mouseX, mouseY, xOffset.toFloat(), modOffset, frameWidth, moduleHeight.toFloat())) {
                    hoveredModule!!.enabled = !hoveredModule!!.enabled
                }
                hoveredModule = null
            }
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button == 0 && draggingCategory != null) { //left mouse button dragging
            val position = categoryPositions[draggingCategory]
            position?.setLocation(mouseX - dragOffsetX, mouseY - dragOffsetY)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        BlurShader.registerRenderCall {
            ShaderRenderUtil.drawRect(0.0, 0.0, width.toDouble(), height.toDouble(), Color(255, 255, 255, 150))
        }
        BlurShader.draw(5)

        for (category in Category.values()) {
            val position = categoryPositions[category] ?: continue

            var xOffset = position.x
            var yOffset = position.y

            RenderUtil.drawRectangle(matrices,
                xOffset.toFloat(), yOffset.toFloat(), frameWidth, categoryHeight, Color(250, 250, 250, 230))
            RenderUtil.drawRectangle(matrices,
                xOffset.toFloat(), yOffset + categoryHeight, frameWidth, frameHeight, Color(250, 250, 250))
            StyledFontRenderer.drawString(matrices, light25, category.name,
                (xOffset + 8).toDouble(), (yOffset + 8).toDouble(), Color(119, 121, 124))

            var modOffset = yOffset + categoryHeight
            for (module in SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(category)) {
                if (modOffset + moduleHeight > yOffset + categoryHeight + frameHeight) {
                    val scissorHeight = (yOffset + categoryHeight + frameHeight - modOffset).toInt()
                    RenderSystem.enableScissor(xOffset.toInt(), modOffset.toInt(), frameWidth.toInt(), scissorHeight)
                }

                val hover = RenderUtil.hovered(mouseX.toDouble(), mouseY.toDouble(),
                    xOffset.toFloat(), modOffset, frameWidth, moduleHeight.toFloat()
                )
                val mouse = GLFW.glfwGetMouseButton(mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
                RenderUtil.drawRectangle(
                    matrices,
                    xOffset.toFloat(),
                    modOffset,
                    frameWidth,
                    moduleHeight.toFloat(),
                    if (module.enabled) {
                        if (hover) {
                            if (mouse) Color(41, 193, 255) else Color(41, 182, 255)
                        } else {
                            Color(41, 166, 255)
                        }
                    } else {
                        if (hover) {
                            if (mouse) Color(221, 221, 221) else Color(231, 231, 231)
                        } else {
                            Color(250, 250, 250)
                        }
                    }
                )

                StyledFontRenderer.drawString(
                    matrices,
                    light20,
                    module.name,
                    (xOffset + (if (module.enabled) 10 else 8)).toDouble(),
                    (modOffset + 2).toDouble(),
                    if (module.enabled) Color.WHITE else Color.BLACK
                )

                modOffset += moduleHeight

                if (modOffset + moduleHeight > yOffset + categoryHeight + frameHeight) {
                    RenderSystem.disableScissor()
                }
            }
        }

        settingGUI?.draw(matrices, mouseX.toDouble(), mouseY.toDouble())
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && settingGUI != null) {
            settingGUI = null
            return false
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}