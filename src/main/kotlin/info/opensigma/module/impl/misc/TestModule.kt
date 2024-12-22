package info.opensigma.module.impl.misc

import info.opensigma.event.impl.render.Render2DEvent
import info.opensigma.module.Module
import info.opensigma.setting.impl.primitive.PrimitiveSetting
import info.opensigma.util.font.Font
import meteordevelopment.orbit.EventHandler
import org.lwjgl.glfw.GLFW

class TestModule : Module("Test", "A module purely for testing purposes",GLFW.GLFW_KEY_V) {

    private val jelloLight = Font("helvetica-neue-light.ttf", 15)

    val testSetting = PrimitiveSetting("Hello", "Purely for testing purposes", true)

    override fun onEnable() {
        if (testSetting.value) {
            println("Hello, my bind is $key")
        } else {
            println("My bind is $key")
        }
    }

    @EventHandler
    fun on2D(event: Render2DEvent) {
        jelloLight.drawString("Hello, world!", 5, 5, -1)
    }

}