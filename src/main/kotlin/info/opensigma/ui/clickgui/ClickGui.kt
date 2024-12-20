package info.opensigma.ui.clickgui

import info.opensigma.module.data.ModuleCategory
import info.opensigma.ui.clickgui.frame.Frame
import info.opensigma.ui.clickgui.frame.impl.CategoryFrame
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class ClickGui : Screen(Text.of("ClickGui")) {

    private val frames = mutableListOf<Frame>()

    init {
        var x = 15f
        var y = 15f
        var count = 0

        ModuleCategory.entries.forEach { category ->
            frames += CategoryFrame(category, x.toDouble(), y.toDouble())

            if (++count == 4) {
                x = 15f
                y += 165f
            } else {
                x += 105f
            }
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) =
        frames.forEach { it.draw(matrices, mouseX, mouseY) }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        frames.any { it.mouseClick(mouseX, mouseY, button) }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        frames.any { it.mouseRelease(mouseX, mouseY, button) }

}