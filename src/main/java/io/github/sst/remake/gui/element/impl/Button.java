package io.github.sst.remake.gui.element.impl;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.newdawn.slick.TrueTypeFont;

public class Button extends Element {
    public float field20584;
    private int field20585 = 0;
    public int field20586 = 0;

    public Button(CustomGuiScreen screen, String iconName, int x, int y, int width, int height) {
        super(screen, iconName, x, y, width, height, false);
    }

    public Button(CustomGuiScreen screen, String iconName, int x, int y, int width, int var6, ColorHelper var7) {
        super(screen, iconName, x, y, width, var6, var7, false);
    }

    public Button(CustomGuiScreen screen, String iconName, int x, int y, int width, int var6, ColorHelper var7, String text) {
        super(screen, iconName, x, y, width, var6, var7, text, false);
    }

    public Button(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, ColorHelper var7, String var8, TrueTypeFont font) {
        super(screen, iconName, x, y, width, height, var7, var8, font, false);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        this.field20584 = this.field20584 + (!this.method13298() ? -0.1F : 0.1F);
        this.field20584 = Math.min(Math.max(0.0F, this.field20584), 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = !this.isHovered() ? 0.3F : (!this.isDragging() ? (!this.method13212() ? Math.max(partialTicks * this.field20584, 0.0F) : 1.5F) : 0.0F);
        int color = ColorHelper.applyAlpha(
                ColorHelper.shiftTowardsOther(this.textColor.getPrimaryColor(), this.textColor.getSecondaryColor(), 1.0F - var4),
                (float) (this.textColor.getPrimaryColor() >> 24 & 0xFF) / 255.0F * partialTicks
        );
        if (this.field20586 <= 0) {
            RenderUtils.drawRoundedRect(
                    (float) this.getXA(),
                    (float) this.getYA(),
                    (float) (this.getXA() + this.getWidthA()),
                    (float) (this.getYA() + this.getHeightA()),
                    color
            );
        } else {
            RenderUtils.drawRoundedButton(
                    (float) this.getXA(), (float) this.getYA(), (float) this.getWidthA(), (float) this.getHeightA(), (float) this.field20586, color
            );
        }

        int var10 = this.getXA()
                + (
                this.textColor.getWidthAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getWidthAlignment() != FontAlignment.RIGHT ? this.getWidthA() / 2 : this.getWidthA())
        );
        int var11 = this.getYA()
                + (
                this.textColor.getHeightAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getHeightAlignment() != FontAlignment.BOTTOM ? this.getHeightA() / 2 : this.getHeightA())
        );
        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.field20585 + var10),
                    (float) var11,
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        super.draw(partialTicks);
    }

    public void method13034(int var1) {
        this.field20585 = var1;
    }

    public int method13035() {
        return this.field20585;
    }
}
