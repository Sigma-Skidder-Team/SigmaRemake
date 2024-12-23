package info.opensigma.module.impl.misc

import info.opensigma.OpenSigma
import info.opensigma.event.impl.render.Render2DEvent
import info.opensigma.module.Module
import info.opensigma.setting.impl.primitive.PrimitiveSetting
import me.x150.renderer.font.FontRenderer
import me.x150.renderer.render.Renderer2d
import meteordevelopment.orbit.EventHandler
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.awt.Font

class TestModule : Module("Test", "A module purely for testing purposes",GLFW.GLFW_KEY_V) {

    val font: Font = Font.decode("SauceCodePro Nerd Font")
    var renderer: FontRenderer? = FontRenderer(font, 10f)
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
        OpenSigma.instance.eventBus.unsubscribe(this)
        renderer?.close()
        renderer = null
    }

    @EventHandler
    fun on2D(event: Render2DEvent) {
        if (renderer == null)
            renderer = FontRenderer(font, 10f)
        renderer?.roundCoordinates(true)
        val theText = Text.literal("The quick brown fox jumps over the lazy dog\n")
            .append(Text.literal("italic\n").styled { it.withItalic(true) })
            .append(Text.literal("bold\n").styled { it.withBold(true) })
            .append(Text.literal("bold italic\n").styled { it.withBold(true).withItalic(true) })
            .append(Text.literal("under\n").styled { it.withUnderline(true) })
            .append(Text.literal("strikethrough\nwith nl\n").styled { it.withStrikethrough(true) })
            .append(Text.literal("Special chars: 1234@æððħſ.ĸ|aa{a}()"));
        var x = 5.0f
        var y = 5.0f
        val width = (renderer as FontRenderer).getTextWidth(theText)
        val height = (renderer as FontRenderer).getTextHeight(theText)
        val mat = event.context.matrices
        mat.push()
//		mat.scale(6, 6, 0)
        Renderer2d.renderQuad(mat, Color.RED, x.toDouble(), y.toDouble(), (x + width).toDouble(), (y + height).toDouble())
        renderer?.drawText(mat, theText, x, y, 1f)
    }

}