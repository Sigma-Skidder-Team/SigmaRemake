package info.opensigma.ui.clickgui.frame

import info.opensigma.system.IMinecraft
import info.opensigma.util.math.GeometryUtils
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable

abstract class Frame : Renderable, IMinecraft {

    open var posX = 0.0
    open var posY = 0.0

    private var dragging = false
    private var dragX = 0.0
    private var dragY = 0.0

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (dragging) {
            posX = mouseX - dragX
            posY = mouseY - dragY
        }

        drawFrame(context, mouseX, mouseY, delta)
    }

    protected abstract fun drawFrame(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float)

    fun mouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (GeometryUtils.isInBounds(mouseX, mouseY, posX, posY, getInteractionBounds()[0], getInteractionBounds()[1], true)) {
            dragX = mouseX - posX
            dragY = mouseY - posY
            dragging = true

            return true
        }

        return mouseClickFrame(mouseX, mouseY, button)
    }

    @Suppress("SameReturnValue")
    protected abstract fun mouseClickFrame(mouseX: Double, mouseY: Double, button: Int): Boolean

    fun mouseRelease(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dragging) {
            dragging = false
            dragX = 0.0
            dragY = 0.0

            return true
        }

        return mouseReleaseFrame(mouseX, mouseY, button)
    }

    @Suppress("SameReturnValue")
    protected abstract fun mouseReleaseFrame(mouseX: Double, mouseY: Double, button: Int): Boolean

    protected abstract fun getInteractionBounds(): DoubleArray

}