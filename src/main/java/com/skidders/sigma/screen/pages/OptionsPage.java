package com.skidders.sigma.screen.pages;

import com.skidders.SigmaReborn;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OptionsPage extends Screen {

    /*
    private final Renderer JelloMediumFont40 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Medium", 40);
    private final Renderer JelloLightFont25 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 25);
    private final Renderer JelloLightFont20 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 20);
    private final Renderer JelloLightFont24 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 24);
    private final Renderer JelloLightFont14 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 14);
    private final Renderer JelloLightFont18 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 18);
     */

    public OptionsPage(String text) {
        super(Text.of(text));
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.renderBackground(matrices);

        //float jelloWidth = JelloMediumFont40.getWidth("Jello");
        //float forSigmaWidth = JelloLightFont25.getWidth("for Sigma");
        //float combinedWidth = jelloWidth + 10 + forSigmaWidth;

       // float x = ((float) width - combinedWidth) / 2;
        float y = ((float) height / 2) - 140;

        //JelloMediumFont40.drawString("Jello", x, y, new Color(254, 254, 254));
        //JelloLightFont25.drawString("for Sigma", x + jelloWidth + 2, y + 7, new Color(220, 220, 220));

        String version = "You're currently using Sigma Reborn";
        //JelloLightFont20.drawString(version, (float) width / 2 - JelloLightFont20.getWidth(version) / 2, y + 28, new Color(110, 111, 112));

        //JelloLightFont20.drawString("GUI Blur:", width / 2 - 80, height / 2 + 40, new Color(136, 137, 136));
        //JelloLightFont20.drawString("GPU Accelerated:", width / 2 - 16, height / 2 + 40, new Color(136, 137, 136));

        String clickguiBound = "Click GUI is currently bound to: " + SigmaReborn.INSTANCE.screenHandler.clickGuiBindName + " Key";
        //JelloLightFont20.drawString(clickguiBound, (float) width / 2 - JelloLightFont20.getWidth(clickguiBound) / 2, (float) height / 2 + 60, new Color(160, 161, 160));
        String configure = "Configure all your keybinds in the keybind manager!";
        //JelloLightFont14.drawString(configure, (float) width / 2 - JelloLightFont14.getWidth(configure) / 2, (float) height / 2 + 75, new Color(103, 105, 103));

        //clickGui.draw(JelloLightFont24, matrices, mouseX, mouseY);
        //keybind.draw(JelloLightFont24, matrices, mouseX, mouseY);
        //credits.draw(JelloLightFont18, matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {

        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
