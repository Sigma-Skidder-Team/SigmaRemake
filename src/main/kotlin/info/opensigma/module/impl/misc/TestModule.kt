package info.opensigma.module.impl.misc

import info.opensigma.module.Module
import info.opensigma.setting.impl.primitive.PrimitiveSetting
import org.lwjgl.glfw.GLFW

class TestModule : Module {

    val testSetting = PrimitiveSetting("Hello", "Purely for testing purposes", true)

    constructor() : super("Test", "A module purely for testing purposes", GLFW.GLFW_KEY_V)

    override fun onEnable() {
        if (testSetting.value) {
            println("Hello, my bind is $key")
        } else {
            println("My bind is $key")
        }
    }

}