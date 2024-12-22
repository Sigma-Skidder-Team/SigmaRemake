package info.opensigma.ui.clickgui.frame.impl

import info.opensigma.ui.clickgui.frame.Frame
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color

class CategoryFrame(
    override var posX: Double,
    override var posY: Double
) : Frame() {
    override fun drawFrame(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(
            posX.toInt(), posY.toInt(),
            (posX + 100).toInt(), (posY + 30).toInt(),
            Color(255, 255, 255, 210).rgb
        )
        context.fill(
            posX.toInt(), (posY + 30).toInt(),
            (posX + 100).toInt(), (posY + 160).toInt(),
            -1
        )
    }

    override fun mouseClickFrame(mouseX: Double, mouseY: Double, button: Int) = false

    override fun mouseReleaseFrame(mouseX: Double, mouseY: Double, button: Int) = false

    override fun getInteractionBounds() = doubleArrayOf(posX, posY, 100.0, 30.0)

}