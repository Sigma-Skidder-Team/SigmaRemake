package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class Image extends Button {
    public static final ColorHelper DEFAULT_COLORS = new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ColorHelper.shiftTowardsBlack(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.1F));
    public Texture texture;

    public Image(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8, String var9, TrueTypeFont var10) {
        super(var1, var2, var3, var4, var5, var6, var8, var9, var10);
        this.texture = var7;
    }

    public Image(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture texture, ColorHelper var8, String var9) {
        super(var1, var2, var3, var4, var5, var6, var8, var9);
        this.texture = texture;
    }

    public Image(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8) {
        super(var1, var2, var3, var4, var5, var6, var8);
        this.texture = var7;
    }

    public Image(GuiComponent screen, String iconName, int x, int y, int width, int height, Texture texture) {
        super(screen, iconName, x, y, width, height, DEFAULT_COLORS);
        this.texture = texture;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.texture.equals(Resources.MORE_ICON) && Client.INSTANCE.notificationManager.isRendering()) {
             return;
        }

        float var4 = !this.isHovered() ? 0.3F : (!this.isDragging() ? (!this.isMouseDownOverComponent() ? Math.max(partialTicks * this.hoverFade, 0.0F) : 1.5F) : 0.0F);
        RenderUtils.drawImage(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) this.getHeight(),
                this.getTexture(),
                ColorHelper.applyAlpha(
                        ColorHelper.shiftTowardsOther(this.textColor.getPrimaryColor(), this.textColor.getSecondaryColor(), 1.0F - var4),
                        (float) (this.textColor.getPrimaryColor() >> 24 & 0xFF) / 255.0F * partialTicks
                )
        );
        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.getX() + this.getWidth() / 2),
                    (float) (this.getY() + this.getHeight() / 2),
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        GL11.glPushMatrix();
        super.drawChildren(partialTicks);
        GL11.glPopMatrix();
    }
}
