package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

public class Checkbox extends InteractiveWidget {
    public boolean value;
    public AnimationUtils animation = new AnimationUtils(70, 90);

    public Checkbox(GuiComponent parent, String label, int x, int y, int width, int height) {
        super(parent, label, x, y, width, height, false);
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.setValue(value, true);
    }

    public void setValue(boolean value, boolean call) {
        if (value != this.getValue()) {
            this.value = value;
            this.animation.changeDirection(!this.value ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
            if (call) {
                this.firePressHandlers();
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        float over = !this.isMouseDownOverComponent() ? 0.43F : 0.6F;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                10.0F,
                ColorHelper.applyAlpha(-4144960, over * this.animation.calcPercent() * partialTicks)
        );
        float scale = (1.0F - this.animation.calcPercent()) * partialTicks;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                10.0F,
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(-14047489, ClientColors.DEEP_TEAL.getColor(), !this.isMouseDownOverComponent() ? 1.0F : 0.9F), scale)
        );
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.getX() + this.getWidth() / 2), (float) (this.getY() + this.getHeight() / 2), 0.0F);
        GL11.glScalef(1.5F - 0.5F * scale, 1.5F - 0.5F * scale, 0.0F);
        GL11.glTranslatef((float) (-this.getX() - this.getWidth() / 2), (float) (-this.getY() - this.getHeight() / 2), 0.0F);
        RenderUtils.drawImage(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                Resources.CHECKMARK,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), scale)
        );
        GL11.glPopMatrix();
        super.draw(partialTicks);
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.setValue(!this.value, true);
    }
}
