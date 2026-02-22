package io.github.sst.remake.gui.framework.widget.internal;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

public class LoadingIndicator extends Widget {
    public TogglableTimer rotationTimer = new TogglableTimer();
    public float hoverAlpha = 0.0F;

    public LoadingIndicator(GuiComponent parent, String text, int x, int y, int width, int height) {
        super(parent, text, x, y, width, height, false);
        this.rotationTimer.start();
    }

    @Override
    public void draw(float partialTicks) {
        this.hoverAlpha = this.hoverAlpha + (this.isHovered() ? 0.2F : -0.2F);
        this.hoverAlpha = Math.min(1.0F, Math.max(0.0F, this.hoverAlpha));
        float rotation = (float) (this.rotationTimer.getElapsedTime() / 75L % 12L);
        if (this.hoverAlpha != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0.0F);
            GL11.glRotatef(rotation * 30.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-this.x - this.width / 2), (float) (-this.y - this.height / 2), 0.0F);
            RenderUtils.drawImage(
                    (float) this.x,
                    (float) this.y,
                    (float) this.width,
                    (float) this.height,
                    Resources.LOADING_INDICATOR,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), this.hoverAlpha * partialTicks)
            );
            GL11.glPopMatrix();
        }
    }
}
