package io.github.sst.remake.gui.screen.options;

import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;

public class OptionsScreen extends Screen {

    private static AnimationUtils anim;
    private static net.minecraft.client.gui.screen.Screen mcScreen = null;

    public OptionsScreen() {
        super("options");

        this.setListening(false);
        int var3 = Math.max((int) ((float) this.height * 0.8F), 420);
        int var4 = (int) ((float) this.width * 0.8F);
        this.addToList(
                new OptionsPage(
                        this, "centerBlock", this.getWidth() - var4, this.getHeight() - var3, var4 - (this.getWidth() - var4), var3 - (this.getHeight() - var3)
                )
        );
        anim = new AnimationUtils(300, 100);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (anim.getDirection() == AnimationUtils.Direction.FORWARDS && anim.calcPercent() == 0.0F && mcScreen != null) {
            MinecraftClient.getInstance().openScreen(mcScreen);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float maxValue = 1.3F - EasingFunctions.easeOutBack(anim.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
        float value = 1.0F;
        if (anim.getDirection() == AnimationUtils.Direction.FORWARDS) {
            maxValue = 0.7F + QuadraticEasing.easeOutQuad(anim.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
            value = anim.calcPercent();
        }

        int color1 = ColorHelper.shiftTowardsOther(-1072689136, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), value);
        int color2 = ColorHelper.shiftTowardsOther(-804253680, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), value);
        RenderUtils.drawGradient(0, 0, this.getWidth(), this.getHeight(), color1, color2);
        this.setScale(maxValue, maxValue);
        this.applyScaleTransforms();
        super.draw(anim.calcPercent());
    }

    public static void showGUI(net.minecraft.client.gui.screen.Screen screen) {
        mcScreen = screen;
        anim.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}
