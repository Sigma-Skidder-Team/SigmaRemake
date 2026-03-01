package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;

import java.awt.*;

public class SaturationValuePanel extends InteractiveWidget {
    public float hue;
    private float saturation;
    private float value;
    public boolean dragging = false;

    public SaturationValuePanel(
            GuiComponent parent,
            String id,
            int x,
            int y,
            int width,
            int height,
            float hue,
            float saturation,
            float value
    ) {
        super(parent, id, x, y, width, height, false);
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.dragging) {
            int localX = this.getMouseX() - this.getAbsoluteX();
            setSaturationFromMouse((float) localX / (float) this.getWidth());

            int localY = this.getMouseY() - this.getAbsoluteY();
            setValueFromMouse(1.0F - (float) localY / (float) this.getHeight());
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int leftColor = ColorHelper.applyAlpha(Color.HSBtoRGB(this.hue, 0.0F, 1.0F), partialTicks);
        int rightColor = ColorHelper.applyAlpha(Color.HSBtoRGB(this.hue, 1.0F, 1.0F), partialTicks);
        int overlayBottom = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks);

        ScissorUtils.startScissor(this);

        RenderUtils.drawColoredQuad(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                leftColor,
                rightColor,
                rightColor,
                leftColor
        );

        RenderUtils.drawColoredQuad(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.0F),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.0F),
                overlayBottom,
                overlayBottom
        );

        RenderUtils.drawLayeredCircle(
                this.x + Math.round((float) this.width * getSaturation()),
                this.y + Math.round((float) this.height * (1.0F - getValue())),
                Color.HSBtoRGB(this.hue, this.saturation, this.value),
                partialTicks
        );

        RenderUtils.drawVerticalDivider(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight()),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.25F * partialTicks)
        );

        ScissorUtils.restoreScissor();
        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        this.dragging = true;
        return super.onMouseDown(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        this.dragging = false;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public void setSaturationFromMouse(float saturation) {
        setSaturation(saturation, true);
    }

    public void setSaturation(float saturation, boolean notify) {
        float clamped = Math.min(Math.max(saturation, 0.0F), 1.0F);
        float previous = this.saturation;
        this.saturation = clamped;

        if (notify && previous != clamped) {
            this.firePressHandlers();
        }
    }

    public float getValue() {
        return this.value;
    }

    public void setValueFromMouse(float value) {
        this.setValue(value, true);
    }

    public void setValue(float value, boolean notify) {
        float clamped = Math.min(Math.max(value, 0.0F), 1.0F);
        float previous = this.value;
        this.value = clamped;

        if (notify && previous != clamped) {
            this.firePressHandlers();
        }
    }

    public int getColorRGB() {
        return Color.HSBtoRGB(this.hue, this.saturation, this.value);
    }
}
