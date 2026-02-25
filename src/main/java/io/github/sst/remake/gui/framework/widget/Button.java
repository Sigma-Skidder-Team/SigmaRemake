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

    public Button(GuiComponent screen, String name, int x, int y, int width, int height) {
        super(screen, name, x, y, width, height, false);
    }

    public Button(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper color) {
        super(screen, name, x, y, width, height, color, false);
    }

    public Button(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper color, String text) {
        super(screen, name, x, y, width, height, color, text, false);
    }

    public Button(
            GuiComponent screen,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper color,
            String text,
            TrueTypeFont font
    ) {
        super(screen, name, x, y, width, height, color, text, font, false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.hoverFade = this.hoverFade + (!this.isHoveredInHierarchy() ? -0.1F : 0.1F);
        this.hoverFade = Math.min(Math.max(0.0F, this.hoverFade), 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float hoverBlendFactor = this.computeHoverBlendFactor(partialTicks);

        int backgroundColor = ColorHelper.applyAlpha(
                ColorHelper.shiftTowardsOther(
                        this.textColor.getPrimaryColor(),
                        this.textColor.getSecondaryColor(),
                        1.0F - hoverBlendFactor
                ),
                ((this.textColor.getPrimaryColor() >> 24) & 0xFF) / 255.0F * partialTicks
        );

        if (this.cornerRadius <= 0) {
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) (this.getX() + this.getWidth()),
                    (float) (this.getY() + this.getHeight()),
                    backgroundColor
            );
        } else {
            RenderUtils.drawRoundedButton(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    (float) this.cornerRadius,
                    backgroundColor
            );
        }

        if (this.getText() != null) {
            int textAnchorX = this.computeTextAnchorX();
            int textAnchorY = this.computeTextAnchorY();

            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.textOffsetX + textAnchorX),
                    (float) textAnchorY,
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

    private float computeHoverBlendFactor(float partialTicks) {
        if (!this.isHovered()) {
            return 0.3F;
        }
        if (this.isDragging()) {
            return 0.0F;
        }
        if (this.isMouseDownOverComponent()) {
            return 1.5F;
        }
        return Math.max(partialTicks * this.hoverFade, 0.0F);
    }

    private int computeTextAnchorX() {
        if (this.textColor.getWidthAlignment() != FontAlignment.CENTER) {
            return this.getX();
        }
        return this.textColor.getWidthAlignment() != FontAlignment.RIGHT
                ? this.getX() + this.getWidth() / 2
                : this.getX() + this.getWidth();
    }

    private int computeTextAnchorY() {
        if (this.textColor.getHeightAlignment() != FontAlignment.CENTER) {
            return this.getY();
        }
        return this.textColor.getHeightAlignment() != FontAlignment.BOTTOM
                ? this.getY() + this.getHeight() / 2
                : this.getY() + this.getHeight();
    }
}
