package info.opensigma.module.impl.misc

import info.opensigma.OpenSigma
import info.opensigma.event.impl.render.Render2DEvent
import info.opensigma.module.Module
import info.opensigma.setting.impl.primitive.PrimitiveSetting
import lexi.fontrenderer.Renderer
import meteordevelopment.orbit.EventHandler
import org.lwjgl.glfw.GLFW

class TestModule : Module("Test", "A module purely for testing purposes",GLFW.GLFW_KEY_V) {

    //private val font: Renderer = OpenSigma.instance.fontManager.getFont("HelveticNeue-Medium", 20)

    val testSetting = PrimitiveSetting("Hello", "Purely for testing purposes", true)

    override fun onEnable() {
        if (testSetting.value) {
            println("Hello, my bind is $key")
        } else {
            println("My bind is $key")
        }
    }

    override fun onDisable() {
        super.onDisable()
        OpenSigma.instance.eventBus.unsubscribe(this::on2D)
    }

    @EventHandler
    fun on2D(event: Render2DEvent) {
        //font.drawString(event.matrices, "Hello, world!", 5, 5, -1)
    }

}