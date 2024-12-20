package info.opensigma.module.impl

import info.opensigma.module.Module
import info.opensigma.ui.clickgui.ClickGui
import org.lwjgl.glfw.GLFW

class ClickGuiModule : Module("ClickGui", "ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT) {

    private val clickGui = ClickGui()

    override fun onEnable() {
        client?.openScreen(clickGui)

        toggle()
    }

}