package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class Button extends InteractiveWidget {
    public float hoverFade;
    private int textOffsetX = 0;
    public int cornerRadius = 0;

    public Button(GuiComponent screen, String iconName, int x, int y, int width, int height) {
        super(screen, iconName, x, y, width, height, false);
    }

    public Button(GuiComponent screen, String iconName, int x, int y, int width, int var6, ColorHelper var7) {
        super(screen, iconName, x, y, width, var6, var7, false);
    }

    public Button(GuiComponent screen, String iconName, int x, int y, int width, int var6, ColorHelper var7, String text) {
        super(screen, iconName, x, y, width, var6, var7, text, false);
    }

    public Button(GuiComponent screen, String iconName, int x, int y, int width, int height, ColorHelper var7, String var8, TrueTypeFont font) {
        super(screen, iconName, x, y, width, height, var7, var8, font, false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.hoverFade = this.hoverFade + (!this.isHoveredInHierarchy() ? -0.1F : 0.1F);
        this.hoverFade = Math.min(Math.max(0.0F, this.hoverFade), 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = !this.isHovered() ? 0.3F : (!this.isDragging() ? (!this.isMouseDownOverComponent() ? Math.max(partialTicks * this.hoverFade, 0.0F) : 1.5F) : 0.0F);
        int color = ColorHelper.applyAlpha(
                ColorHelper.shiftTowardsOther(this.textColor.getPrimaryColor(), this.textColor.getSecondaryColor(), 1.0F - var4),
                (float) (this.textColor.getPrimaryColor() >> 24 & 0xFF) / 255.0F * partialTicks
        );
        if (this.cornerRadius <= 0) {
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) (this.getX() + this.getWidth()),
                    (float) (this.getY() + this.getHeight()),
                    color
            );
        } else {
            RenderUtils.drawRoundedButton(
                    (float) this.getX(), (float) this.getY(), (float) this.getWidth(), (float) this.getHeight(), (float) this.cornerRadius, color
            );
        }

        int var10 = this.getX()
                + (
                this.textColor.getWidthAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getWidthAlignment() != FontAlignment.RIGHT ? this.getWidth() / 2 : this.getWidth())
        );
        int var11 = this.getY()
                + (
                this.textColor.getHeightAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getHeightAlignment() != FontAlignment.BOTTOM ? this.getHeight() / 2 : this.getHeight())
        );
        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.textOffsetX + var10),
                    (float) var11,
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        super.draw(partialTicks);
    }

    public void setTextOffsetX(int offset) {
        this.textOffsetX = offset;
    }

    public int getTextOffsetX() {
        return this.textOffsetX;
    }
}
