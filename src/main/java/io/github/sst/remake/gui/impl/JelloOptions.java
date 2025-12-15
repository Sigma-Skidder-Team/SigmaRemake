package io.github.sst.remake.gui.impl;

import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.jello.JelloOptionsGroup;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import org.newdawn.slick.opengl.Texture;
import org.lwjgl.opengl.GL11;

public class JelloOptions extends Screen {
    private int field21109 = 0;
    private int field21110 = 0;
    private boolean field21111 = true;
    public static AnimationUtils field21112 = new AnimationUtils(300, 200);
    private Texture field21113;
    private final JelloOptionsGroup field21114;
    public static net.minecraft.client.gui.screen.Screen field21115 = null;

    public JelloOptions() {
        super("options");

        this.setListening(false);
        int var3 = Math.max((int) ((float) this.height * 0.8F), 420);
        int var4 = (int) ((float) this.width * 0.8F);
        this.addToList(
                this.field21114 = new JelloOptionsGroup(
                        this, "centerBlock", this.getWidth() - var4, this.getHeight() - var3, var4 - (this.getWidth() - var4), var3 - (this.getHeight() - var3)
                )
        );
        field21112 = new AnimationUtils(300, 100);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (field21112.getDirection() == AnimationUtils.Direction.BACKWARDS && field21112.calcPercent() == 0.0F && field21115 != null) {
            MinecraftClient.getInstance().openScreen(field21115);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = 1.3F - EasingFunctions.easeOutBack(field21112.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
        float var5 = 1.0F;
        if (field21112.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var4 = 0.7F + QuadraticEasing.easeOutQuad(field21112.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
            var5 = field21112.calcPercent();
        }

        int var6 = ColorHelper.shiftTowardsOther(-1072689136, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), var5);
        int var7 = ColorHelper.shiftTowardsOther(-804253680, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F), var5);
        RenderUtils.method11431(0, 0, this.getWidth(), this.getHeight(), var6, var7);
        this.method13279(var4, var4);
        this.method13224();
        super.draw(field21112.calcPercent());
    }

    private void method13437(float var1) {
        int var4 = this.getMouseX() * -1;
        float var5 = (float) this.getMouseY() / (float) this.getWidth() * -114.0F;
        if (this.field21111) {
            this.field21109 = (int) var5;
            this.field21110 = var4;
            this.field21111 = false;
        }

        float var6 = var5 - (float) this.field21109;
        float var7 = (float) (var4 - this.field21110);
        GL11.glPushMatrix();
        if (this.field21113 != null) {
            RenderUtils.drawTexture(
                    (float) this.field21110,
                    (float) this.field21109,
                    (float) (this.getWidth() * 2),
                    (float) (this.getHeight() + 114),
                    this.field21113,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
            );
        }

        GL11.glPopMatrix();
        float var8 = 0.5F;
        if (var5 != (float) this.field21109) {
            this.field21109 = (int) ((float) this.field21109 + var6 * var8);
        }

        if (var4 != this.field21110) {
            this.field21110 = (int) ((float) this.field21110 + var7 * var8);
        }
    }

    public static void showGUI(net.minecraft.client.gui.screen.Screen var0) {
        field21115 = var0;
        field21112.changeDirection(AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}
