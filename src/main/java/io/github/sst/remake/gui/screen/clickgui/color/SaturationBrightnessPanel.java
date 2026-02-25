package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;

import java.awt.*;

public class SaturationBrightnessPanel extends InteractiveWidget {
    public float hue;
    private float saturation = 0.0F;
    private float brightness = 1.0F;
    public boolean dragging = false;

    public SaturationBrightnessPanel(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, float var7, float var8, float var9) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.hue = var7;
        this.saturation = var8;
        this.brightness = var9;
    }

    public void setHue(float var1) {
        this.hue = var1;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.dragging) {
            int var5 = this.getMouseX() - this.getAbsoluteX();
            this.setSaturationFromMouse((float) var5 / (float) this.getWidth());
            int var6 = this.getMouseY() - this.getAbsoluteY();
            this.setBrightnessFromMouse(1.0F - (float) var6 / (float) this.getHeight());
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = ColorHelper.applyAlpha(Color.HSBtoRGB(this.hue, 0.0F, 1.0F), partialTicks);
        int var5 = ColorHelper.applyAlpha(Color.HSBtoRGB(this.hue, 1.0F, 1.0F), partialTicks);
        int var6 = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks);
        ScissorUtils.startScissor(this);
        RenderUtils.drawColoredQuad(
                this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), var4, var5, var5, var4
        );
        RenderUtils.drawColoredQuad(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.0F),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.0F),
                var6,
                var6
        );
        ColorPicker.drawLayeredCircle(
                this.x + Math.round((float) this.width * this.getSaturation()),
                this.y + Math.round((float) this.height * (1.0F - this.getBrightness())),
                Color.HSBtoRGB(this.hue, this.saturation, this.brightness),
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

    public void setSaturationFromMouse(float var1) {
        this.setSaturation(var1, true);
    }

    public void setSaturation(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.saturation;
        this.saturation = var1;
        if (var2 && var5 != var1) {
            this.firePressHandlers();
        }
    }

    public float getBrightness() {
        return this.brightness;
    }

    public void setBrightnessFromMouse(float var1) {
        this.setBrightness(var1, true);
    }

    public void setBrightness(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.brightness;
        this.brightness = var1;
        if (var2 && var5 != var1) {
            this.firePressHandlers();
        }
    }

    public int getColorRGB() {
        return Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
    }
}
