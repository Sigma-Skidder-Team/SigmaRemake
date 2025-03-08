package com.skidders.sigma.screens.pages

import com.skidders.SigmaReborn
import com.skidders.sigma.screens.clickgui.components.ButtonLineComponent
import com.skidders.sigma.screens.clickgui.components.CheckboxComponent
import com.skidders.sigma.utils.misc.MouseHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.awt.Color

class OptionsPage(text: String) : Screen(Text.of(text)) {

//    private val jelloMediumFont40 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Medium", 40)
//    private val jelloLightFont25 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 25)
//    private val jelloLightFont20 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 20)
//    private val jelloLightFont24 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 24)
//    private val jelloLightFont14 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 14)
//    private val jelloLightFont18 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 18)

    private lateinit var clickGui: ButtonLineComponent
    private lateinit var keybind: ButtonLineComponent
    private lateinit var credits: ButtonLineComponent

    private lateinit var mouseHandler: MouseHandler

    private val guiBlur = CheckboxComponent()
    private val gpuAccel = CheckboxComponent()

    override fun init() {
        super.init()

        keybind = ButtonLineComponent("Open Keybind Manager",
            (width / 2 - 80).toFloat(), (height / 2 + 113).toFloat(), Color(254, 254, 254), this)
        clickGui = ButtonLineComponent("Open Jello's Click GUI",
            (width / 2 + 80).toFloat(), (height / 2 + 113).toFloat(), Color(254, 254, 254), this)
        credits = ButtonLineComponent("Credits", (width / 2).toFloat(),
            (height / 2 + 20).toFloat(), Color(254, 254, 254), this)
        mouseHandler = MouseHandler(MinecraftClient.getInstance().window.handle)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        renderBackground(matrices)

        //float jelloWidth = jelloMediumFont40.getWidth("Jello");
        //float forSigmaWidth = jelloLightFont25.getWidth("for Sigma");
        //float combinedWidth = jelloWidth + 10 + forSigmaWidth;

        //float x = (width - combinedWidth) / 2f
       // float x = ((float) width - combinedWidth) / 2;
        val y = (height.toFloat() / 2) - 140;

        //JelloMediumFont40.drawString("Jello", x, y, new Color(254, 254, 254));
        //JelloLightFont25.drawString("for Sigma", x + jelloWidth + 2, y + 7, new Color(220, 220, 220));

        val version = "You're currently using Sigma Reborn";
        //JelloLightFont20.drawString(version, (float) width / 2 - JelloLightFont20.getWidth(version) / 2, y + 28, new Color(110, 111, 112));

        //JelloLightFont20.drawString("GUI Blur:", width / 2 - 80, height / 2 + 40, new Color(136, 137, 136));
        //JelloLightFont20.drawString("GPU Accelerated:", width / 2 - 16, height / 2 + 40, new Color(136, 137, 136));

        SigmaReborn.INSTANCE.screenProcessor.guiBlur = guiBlur.draw(mouseHandler, SigmaReborn.INSTANCE.screenProcessor.guiBlur,
            mouseX.toDouble(), mouseY.toDouble(), (width / 2 - 38).toFloat(), height / 2 + 39.5f);
        SigmaReborn.INSTANCE.screenProcessor.gpuAccelerated = gpuAccel.draw(mouseHandler, SigmaReborn.INSTANCE.screenProcessor.gpuAccelerated,
            mouseX.toDouble(), mouseY.toDouble(), (width / 2 + 63).toFloat(), height / 2 + 39.5f);

        val clickguiBound = "Click GUI is currently bound to: " + SigmaReborn.INSTANCE.screenProcessor.clickGuiBindName + " Key";
        //JelloLightFont20.drawString(clickguiBound, (float) width / 2 - JelloLightFont20.getWidth(clickguiBound) / 2, (float) height / 2 + 60, new Color(160, 161, 160));
        val configure = "Configure all your keybinds in the keybind manager!";
        //JelloLightFont14.drawString(configure, (float) width / 2 - JelloLightFont14.getWidth(configure) / 2, (float) height / 2 + 75, new Color(103, 105, 103));

        //clickGui.draw(JelloLightFont24, matrices, mouseX, mouseY);
        //keybind.draw(JelloLightFont24, matrices, mouseX, mouseY);
        //credits.draw(JelloLightFont18, matrices, mouseX, mouseY);
    }

    public override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            if (clickGui.click(mouseX, mouseY, button)) {
                MinecraftClient.getInstance().openScreen(SigmaReborn.INSTANCE.screenProcessor.clickGUI);
            }

//            if (keybind.click(mouseX, mouseY, button)) {
//
//            }
//
//            if (credits.click(mouseX, mouseY, button)) {
//
//            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}