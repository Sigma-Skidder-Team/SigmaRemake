package io.github.sst.remake.gui.element.impl;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

public class Checkbox extends Element {
    public boolean value;
    public AnimationUtils field21370 = new AnimationUtils(70, 90);

    public Checkbox(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
    }

    public boolean getValue() {
        return this.value;
    }

    public void method13704(boolean var1) {
        this.method13705(var1, true);
    }

    public void method13705(boolean value, boolean var2) {
        if (value != this.getValue()) {
            this.value = value;
            this.field21370.changeDirection(!this.value ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
            if (var2) {
                this.callUIHandlers();
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = !this.isMouseDownOverComponent() ? 0.43F : 0.6F;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                10.0F,
                ColorHelper.applyAlpha(-4144960, var4 * this.field21370.calcPercent() * partialTicks)
        );
        float var5 = (1.0F - this.field21370.calcPercent()) * partialTicks;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                10.0F,
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(-14047489, ClientColors.DEEP_TEAL.getColor(), !this.isMouseDownOverComponent() ? 1.0F : 0.9F), var5)
        );
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.getX() + this.getWidth() / 2), (float) (this.getY() + this.getHeight() / 2), 0.0F);
        GL11.glScalef(1.5F - 0.5F * var5, 1.5F - 0.5F * var5, 0.0F);
        GL11.glTranslatef((float) (-this.getX() - this.getWidth() / 2), (float) (-this.getY() - this.getHeight() / 2), 0.0F);
        RenderUtils.drawImage(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                Resources.checkPNG,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5)
        );
        GL11.glPopMatrix();
        var5 *= var5;
        super.draw(partialTicks);
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.method13705(!this.value, true);
    }
}
