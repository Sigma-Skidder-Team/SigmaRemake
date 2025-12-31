package io.github.sst.remake.gui.element.impl;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class TextButton extends Element {
    public AnimationUtils lineAnim;

    public TextButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
        int var12 = (int) (210.0 * Math.sqrt((float) var5 / 242.0F));
        this.lineAnim = new AnimationUtils(var12, var12);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.lineAnim.changeDirection(!this.method13298() ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.getText() != null) {
            int var4 = this.textColor.getPrimaryColor();
            int var5 = this.getX()
                    + (
                    this.textColor.getWidthAlignment() != FontAlignment.CENTER
                            ? 0
                            : (this.textColor.getWidthAlignment() != FontAlignment.RIGHT ? this.getWidth() / 2 : this.getWidth())
            );
            int var6 = this.getY()
                    + (
                    this.textColor.getHeightAlignment() != FontAlignment.CENTER
                            ? 0
                            : (this.textColor.getHeightAlignment() != FontAlignment.BOTTOM ? this.getHeight() / 2 : this.getHeight())
            );
            int var7 = this.getFont().getWidth(this.getText());
            float var8 = 18;
            float var9 = (float) Math.pow(this.lineAnim.calcPercent(), 3.0);
            RenderUtils.drawString(
                    this.getFont(),
                    (float) var5,
                    (float) var6,
                    this.getText(),
                    ColorHelper.applyAlpha(var4, partialTicks * ColorHelper.getAlpha(var4)),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
            RenderUtils.drawRoundedRect(
                    (float) var5 - (float) (var7 / 2) * var9,
                    var6 + var8,
                    (float) var5 + (float) (var7 / 2) * var9,
                    var6 + var8 + 2,
                    ColorHelper.applyAlpha(var4, partialTicks * ColorHelper.getAlpha(var4))
            );
            super.draw(partialTicks);
        }
    }
}
