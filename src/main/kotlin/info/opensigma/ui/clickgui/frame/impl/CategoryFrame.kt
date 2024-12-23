package info.opensigma.ui.clickgui.frame.impl

import com.mojang.blaze3d.systems.RenderSystem
import info.opensigma.ui.clickgui.frame.Frame
import me.x150.renderer.font.FontRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.awt.Color
import java.awt.Font

class CategoryFrame(
    override var posX: Double,
    override var posY: Double
) : Frame() {
    val font: Font = Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/assets/opensigma/fonts/HelveticaNeue-Light.ttf"))
    var renderer: FontRenderer = FontRenderer(font, 25f)

    override fun drawFrame(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(
            posX.toInt(), posY.toInt(),
            (posX + 100).toInt(), (posY + 30).toInt(),
            Color(23, 23, 23, 120).rgb
        )
        renderer.drawText(
            context.matrices,
            Text.of("Category"),
            (posX + 5).toFloat(), (posY).toFloat(),
            1f
        )
        context.fill(
            posX.toInt(), (posY + 30).toInt(),
            (posX + 100).toInt(), (posY + 160).toInt(),
            Color(32, 32, 32, 180).rgb
        )
    }

    override fun mouseClickFrame(mouseX: Double, mouseY: Double, button: Int) = false

    override fun mouseReleaseFrame(mouseX: Double, mouseY: Double, button: Int) = false

    override fun getInteractionBounds() = doubleArrayOf(posX, posY, 100.0, 30.0)

}