package info.opensigma.ui.clickgui.frame

import info.opensigma.system.IMinecraft
import info.opensigma.util.math.GeometryUtils
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack

abstract class Frame : DrawableHelper(), IMinecraft {

    open var posX = 0.0
    open var posY = 0.0

    private var dragging = false
    private var dragX = 0.0
    private var dragY = 0.0

    fun draw(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        if (dragging) {
            posX = mouseX - dragX
            posY = mouseY - dragY
        }

        drawFrame(matrices, mouseX, mouseY)
    }

    protected abstract fun drawFrame(matrices: MatrixStack, mouseX: Int, mouseY: Int)

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