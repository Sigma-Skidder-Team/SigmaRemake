package io.github.sst.remake.gui.screen.mainmenu;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Image;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.BufferUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class RoundButton extends Image {
    public boolean field20577 = false;
    public AnimationUtils field20578 = new AnimationUtils(160, 140, AnimationUtils.Direction.FORWARDS);

    public RoundButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8, String var9, TrueTypeFont var10) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
    }

    public RoundButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8, String var9) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, var9);
    }

    public RoundButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8) {
        super(var1, var2, var3, var4, var5, var6, var7, var8);
    }

    public RoundButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Texture var7) {
        super(var1, var2, var3, var4, var5, var6, var7, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor()));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.field20577 = this.isHoveredInHierarchy();
        if (!this.field20577) {
            if (this.method13029()) {
                this.field20578.changeDirection(AnimationUtils.Direction.FORWARDS);
                this.setReAddChildren(false);
            }
        } else {
            this.field20578.changeDirection(AnimationUtils.Direction.BACKWARDS);
            this.setReAddChildren(true);
        }
    }

    public boolean method13029() {
        return Math.abs(this.method13030() - this.method13031()) < 0.6F;
    }

    public float method13030() {
        return VecUtils.interpolate(this.field20578.calcPercent(), 0.24, 0.88, 0.3, 1.0);
    }

    public float method13031() {
        return VecUtils.interpolate(this.field20578.calcPercent(), 0.45, 0.02, 0.59, 0.28);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = !this.isMouseDownOverComponent() ? 0.0F : 0.1F;
        float var5 = this.method13030();
        if (this.field20578.getDirection() == AnimationUtils.Direction.FORWARDS) {
            var5 = this.method13031();
        }

        int var6 = (int) ((double) this.getWidth() * (1.0 + (double) var5 * 0.2));
        int var7 = (int) ((double) this.getHeight() * (1.0 + (double) var5 * 0.2));
        int var8 = this.getX() - (var6 - this.getWidth()) / 2;
        int var9 = (int) ((double) (this.getY() - (var7 - this.getHeight()) / 2) - (double) ((float) (this.getHeight() / 2) * var5) * 0.2);
        float[] var10 = BufferUtils.calculateAspectRatioFit(this.getTexture().getWidth(), this.getTexture().getHeight(), (float) var6, (float) var7);
        float var11 = 85;
        RenderUtils.drawImage(
                (float) var8 + var10[0] - var11,
                (float) var9 + var10[1] - var11,
                var10[2] + (var11 * 2),
                var10[3] + (var11 * 2),
                Resources.shadowPNG,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field20578.calcPercent() * 0.7F * partialTicks)
        );
        RenderUtils.drawImage(
                (float) var8 + var10[0],
                (float) var9 + var10[1],
                var10[2],
                var10[3],
                this.getTexture(),
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(this.textColor.getPrimaryColor(), this.textColor.getSecondaryColor(), 1.0F - var4), partialTicks)
        );
        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (var8 + var6 / 2),
                    (float) (var9 + var7 / 2),
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        TrueTypeFont font = this.getFont();
        float var13 = 0.8F + var5 * 0.2F;
        if (var5 > 0.0F) {
            GL11.glPushMatrix();
            String var14 = this.getText() != null ? this.getText() : this.name;
            GL11.glTranslatef(
                    (float) (this.getX() + this.getWidth() / 2 - font.getWidth(var14) / 2), (float) (this.getY() + this.getHeight() - 40), 0.0F
            );
            GL11.glScalef(var13, var13, var13);
            GL11.glAlphaFunc(519, 0.0F);
            RenderUtils.drawImage(
                    (1.0F - var13) * (float) font.getWidth(var14) / 2.0F + 1.0F - (float) font.getWidth(var14) / 2.0F,
                    (float) font.getHeight(var14) / 3.0F,
                    (float) (font.getWidth(var14) * 2),
                    (float) font.getHeight(var14) * 3.0F,
                    Resources.shadowPNG,
                    var5 * 0.6F * partialTicks
            );
            RenderUtils.drawString(
                    font,
                    (1.0F - var13) * (float) font.getWidth(var14) / 2.0F + 1.0F,
                    40.0F,
                    var14,
                    ColorHelper.applyAlpha(this.getTextColor().getPrimaryColor(), var5 * 0.6F * partialTicks)
            );
            GL11.glPopMatrix();
        }

        super.drawChildren(partialTicks);
    }
}
