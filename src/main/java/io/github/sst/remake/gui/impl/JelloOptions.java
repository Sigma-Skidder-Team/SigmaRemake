package io.github.sst.remake.gui.impl;

import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.options.OptionGroup;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;

public class JelloOptions extends Screen {

    private static final AnimationUtils anim = new AnimationUtils(300, 100);
    private static net.minecraft.client.gui.screen.Screen field21115 = null;

    public JelloOptions() {
        super("options");

        this.setListening(false);
        int var3 = Math.max((int) ((float) this.height * 0.8F), 420);
        int var4 = (int) ((float) this.width * 0.8F);
        this.addToList(
                new OptionGroup(
                        this, "centerBlock", this.getWidth() - var4, this.getHeight() - var3, var4 - (this.getWidth() - var4), var3 - (this.getHeight() - var3)
                )
        );
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (anim.getDirection() == AnimationUtils.Direction.BACKWARDS && anim.calcPercent() == 0.0F && field21115 != null) {
            MinecraftClient.getInstance().openScreen(field21115);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = 1.3F - EasingFunctions.easeOutBack(anim.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
        float var5 = 1.0F;
        if (anim.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var4 = 0.7F + QuadraticEasing.easeOutQuad(anim.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
            var5 = anim.calcPercent();
        }

        int var6 = ColorHelper.shiftTowardsOther(-1072689136, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), var5);
        int var7 = ColorHelper.shiftTowardsOther(-804253680, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), var5);
        RenderUtils.method11431(0, 0, this.getWidth(), this.getHeight(), var6, var7);
        this.method13279(var4, var4);
        this.method13224();
        super.draw(anim.calcPercent());
    }

    public static void showGUI(net.minecraft.client.gui.screen.Screen var0) {
        field21115 = var0;
        anim.changeDirection(AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}
