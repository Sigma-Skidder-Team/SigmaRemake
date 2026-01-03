package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.awt.*;

public class ColorPickerSlider extends InteractiveWidget {
    private static String[] field20602;
    private float field20679;
    public boolean field20680 = false;

    public ColorPickerSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, float var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20679 = var7;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        int var5 = this.getMouseX() - this.getAbsoluteX();
        if (this.field20680) {
            this.method13097((float) var5 / (float) this.getWidth());
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
                this.x + Math.round((float) this.width * this.field20679) + 1, this.y + 4, Color.HSBtoRGB(this.field20679, 1.0F, 1.0F), partialTicks
        );
        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        this.field20680 = true;
        return super.onMouseDown(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        this.field20680 = false;
    }

    public float method13096() {
        return this.field20679;
    }

    public void method13097(float var1) {
        this.method13098(var1, true);
    }

    public void method13098(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.field20679;
        this.field20679 = var1;
        if (var2 && var5 != var1) {
            this.callUIHandlers();
        }
    }
}
