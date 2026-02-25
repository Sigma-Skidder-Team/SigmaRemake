package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.awt.*;

public class ColorPicker extends InteractiveWidget {
    public int colorValue;
    public boolean rainbowEnabled;
    public SaturationBrightnessPanel saturationBrightnessPanel;
    public HueSlider hueSlider;
    public ColorPreview preview;

    public ColorPicker(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.colorValue = var7;
        Color var11 = new Color(var7);
        float[] var12 = Color.RGBtoHSB(var11.getRed(), var11.getGreen(), var11.getBlue(), null);
        this.addToList(this.saturationBrightnessPanel = new SaturationBrightnessPanel(this, "block", 10, 10, var5 - 20, var6 - 50, var12[0], var12[1], var12[2]));
        this.addToList(this.hueSlider = new HueSlider(this, "slider", 14, var6 - 25, var5 - 65, 8, var12[0]));
        this.addToList(this.preview = new ColorPreview(this, "bubble", var5 - 40, var6 - 32, 25, 25, var11.getRGB()));
        this.saturationBrightnessPanel.onPress(interactiveWidget -> this.updateColorAndNotify());
        this.hueSlider.onPress(interactiveWidget -> this.updateColorAndNotify());
        this.preview.onClick((parent, mouseButton) -> this.toggleRainbow(!this.getRainbow()));
        this.rainbowEnabled = var8;
    }

    public void toggleRainbow(boolean var1) {
        this.setRainbow(var1);
        this.firePressHandlers();
    }

    public void setRainbow(boolean var1) {
        this.rainbowEnabled = var1;
    }

    public boolean getRainbow() {
        return this.rainbowEnabled;
    }

    public void setValue(int var1) {
        if (var1 != this.colorValue) {
            Color var4 = new Color(var1);
            float[] var5 = Color.RGBtoHSB(var4.getRed(), var4.getGreen(), var4.getBlue(), null);
            this.saturationBrightnessPanel.setHue(var5[0]);
            this.saturationBrightnessPanel.setSaturation(var5[1], false);
            this.saturationBrightnessPanel.setBrightness(var5[2], false);
            this.hueSlider.setHue(var5[0], false);
            this.preview.colorValue = var1;
        }
    }

    public int getValue() {
        return this.colorValue;
    }

    private void updateColorAndNotify() {
        this.updateColorFromControls();
        this.firePressHandlers();
    }

    private void updateColorFromControls() {
        float var3 = this.hueSlider.getHue();
        this.saturationBrightnessPanel.setHue(var3);
        this.colorValue = this.saturationBrightnessPanel.getColorRGB();
        this.preview.colorValue = this.colorValue;
    }

    public static void drawLayeredCircle(int var0, int var1, int var2, float var3) {
        RenderUtils.drawCircle((float) var0, (float) var1, (float) 14, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F * var3));
        RenderUtils.drawCircle((float) var0, (float) var1, (float) (14 - 1), ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.14F * var3));
        RenderUtils.drawCircle((float) var0, (float) var1, (float) (14 - 2), ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var3));
        RenderUtils.drawCircle((float) var0, (float) var1, (float) (14 - 6), ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(var2, ClientColors.DEEP_TEAL.getColor(), 0.7F), var3));
        RenderUtils.drawCircle((float) var0, (float) var1, (float) (14 - 7), ColorHelper.applyAlpha(var2, var3));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.rainbowEnabled) {
            this.hueSlider.setHue((float) (System.currentTimeMillis() % 4000L) / 4000.0F, false);
            this.updateColorFromControls();
        }

        super.draw(partialTicks);
    }
}
