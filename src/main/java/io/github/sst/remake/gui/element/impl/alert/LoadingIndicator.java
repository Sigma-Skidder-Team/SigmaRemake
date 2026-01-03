package io.github.sst.remake.gui.element.impl.alert;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.Widget;
import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

public class LoadingIndicator extends Widget {
    private static String[] field20736;
    public TimerUtils field20769 = new TimerUtils();
    public float field20770 = 0.0F;

    public LoadingIndicator(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20769.start();
    }

    @Override
    public void draw(float partialTicks) {
        this.field20770 = this.field20770 + (this.isHovered() ? 0.2F : -0.2F);
        this.field20770 = Math.min(1.0F, Math.max(0.0F, this.field20770));
        float var4 = (float) (this.field20769.getElapsedTime() / 75L % 12L);
        if (this.field20770 != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0.0F);
            GL11.glRotatef(var4 * 30.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-this.x - this.width / 2), (float) (-this.y - this.height / 2), 0.0F);
            RenderUtils.drawImage(
                    (float) this.x,
                    (float) this.y,
                    (float) this.width,
                    (float) this.height,
                    Resources.loadingIndicatorPNG,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), this.field20770 * partialTicks)
            );
            GL11.glPopMatrix();
        }
    }
}
