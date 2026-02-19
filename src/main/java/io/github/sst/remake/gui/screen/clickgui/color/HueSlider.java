package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.awt.*;

public class HueSlider extends InteractiveWidget {
    private float hue;
    public boolean dragging = false;

    public HueSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, float var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.hue = var7;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        int var5 = this.getMouseX() - this.getAbsoluteX();
        if (this.dragging) {
            this.setHueFromMouse((float) var5 / (float) this.getWidth());
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        for (int var4 = 0; var4 < this.width; var4++) {
            float var5 = (float) var4 / (float) this.width;
            RenderUtils.drawRoundedRect2(
                    (float) (this.x + var4),
                    (float) this.y,
                    1.0F,
                    (float) this.height,
                    ColorHelper.applyAlpha(Color.HSBtoRGB(var5, 1.0F, 1.0F), partialTicks)
            );
        }

        RenderUtils.drawVerticalDivider(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight()),
                ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.5F * partialTicks)
        );
        ColorPicker.drawLayeredCircle(
                this.x + Math.round((float) this.width * this.hue) + 1, this.y + 4, Color.HSBtoRGB(this.hue, 1.0F, 1.0F), partialTicks
        );
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

    public float getHue() {
        return this.hue;
    }

    public void setHueFromMouse(float var1) {
        this.setHue(var1, true);
    }

    public void setHue(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.hue;
        this.hue = var1;
        if (var2 && var5 != var1) {
            this.callUIHandlers();
        }
    }
}
