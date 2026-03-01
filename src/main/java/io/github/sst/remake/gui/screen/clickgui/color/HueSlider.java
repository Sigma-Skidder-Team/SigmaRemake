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

    public HueSlider(GuiComponent parent, String name, int x, int y, int width, int height, float initialHue) {
        super(parent, name, x, y, width, height, false);
        this.hue = initialHue;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        int localMouseX = this.getMouseX() - this.getAbsoluteX();

        if (this.dragging) {
            this.setHueFromMouse((float) localMouseX / (float) this.getWidth());
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        for (int pixelX = 0; pixelX < this.width; pixelX++) {
            float pixelHue = (float) pixelX / (float) this.width;

            RenderUtils.drawRoundedRect2(
                    (float) (this.x + pixelX),
                    (float) this.y,
                    1.0F,
                    (float) this.height,
                    ColorHelper.applyAlpha(Color.HSBtoRGB(pixelHue, 1.0F, 1.0F), partialTicks)
            );
        }

        RenderUtils.drawVerticalDivider(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight()),
                ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.5F * partialTicks)
        );

        int thumbX = this.x + Math.round((float) this.width * this.hue) + 1;
        int thumbY = this.y + 4;
        RenderUtils.drawLayeredCircle(thumbX, thumbY, Color.HSBtoRGB(this.hue, 1.0F, 1.0F), partialTicks);

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

    public void setHueFromMouse(float mousePercent) {
        this.setHue(mousePercent, true);
    }

    public void setHue(float hue, boolean notify) {
        float clampedHue = Math.min(Math.max(hue, 0.0F), 1.0F);

        float previousHue = this.hue;
        this.hue = clampedHue;

        if (notify && previousHue != clampedHue) {
            this.firePressHandlers();
        }
    }
}
