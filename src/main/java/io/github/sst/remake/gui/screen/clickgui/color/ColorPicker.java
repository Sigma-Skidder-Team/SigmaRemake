package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;

import java.awt.*;

public class ColorPicker extends InteractiveWidget {
    public int selectedColor;
    public boolean rainbowEnabled;

    public SaturationBrightnessPanel saturationBrightnessPanel;
    public HueSlider hueSlider;
    public ColorPreview preview;

    public ColorPicker(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            int initialColor,
            boolean rainbowEnabled
    ) {
        super(parent, name, x, y, width, height, false);

        this.selectedColor = initialColor;

        Color initialAwtColor = new Color(initialColor);
        float[] hsb = Color.RGBtoHSB(
                initialAwtColor.getRed(),
                initialAwtColor.getGreen(),
                initialAwtColor.getBlue(),
                null
        );

        this.saturationBrightnessPanel = new SaturationBrightnessPanel(
                this,
                "block",
                10,
                10,
                width - 20,
                height - 50,
                hsb[0],
                hsb[1],
                hsb[2]
        );
        this.addToList(this.saturationBrightnessPanel);

        this.hueSlider = new HueSlider(
                this,
                "slider",
                14,
                height - 25,
                width - 65,
                8,
                hsb[0]
        );
        this.addToList(this.hueSlider);

        this.preview = new ColorPreview(
                this,
                "bubble",
                width - 40,
                height - 32,
                25,
                25,
                initialAwtColor.getRGB()
        );
        this.addToList(this.preview);

        this.saturationBrightnessPanel.onPress(interactiveWidget -> this.updateColorAndNotify());
        this.hueSlider.onPress(interactiveWidget -> this.updateColorAndNotify());

        // Clicking the preview toggles rainbow mode.
        this.preview.onClick((clicked, mouseButton) -> this.toggleRainbow(!this.isRainbowEnabled()));

        this.rainbowEnabled = rainbowEnabled;
    }

    public void toggleRainbow(boolean enabled) {
        this.setRainbowEnabled(enabled);
        this.firePressHandlers();
    }

    public void setRainbowEnabled(boolean enabled) {
        this.rainbowEnabled = enabled;
    }

    public boolean isRainbowEnabled() {
        return this.rainbowEnabled;
    }

    public void setValue(int color) {
        if (color != this.selectedColor) {
            Color awtColor = new Color(color);
            float[] hsb = Color.RGBtoHSB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(), null);

            this.saturationBrightnessPanel.setHue(hsb[0]);
            this.saturationBrightnessPanel.setSaturation(hsb[1], false);
            this.saturationBrightnessPanel.setBrightness(hsb[2], false);

            this.hueSlider.setHue(hsb[0], false);

            this.preview.previewColor = color;
        }
    }

    public int getValue() {
        return this.selectedColor;
    }

    private void updateColorAndNotify() {
        this.updateColorFromControls();
        this.firePressHandlers();
    }

    private void updateColorFromControls() {
        float hue = this.hueSlider.getHue();

        this.saturationBrightnessPanel.setHue(hue);

        this.selectedColor = this.saturationBrightnessPanel.getColorRGB();
        this.preview.previewColor = this.selectedColor;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.rainbowEnabled) {
            float hue = (float) (System.currentTimeMillis() % 4000L) / 4000.0F;
            this.hueSlider.setHue(hue, false);
            this.updateColorFromControls();
        }

        super.draw(partialTicks);
    }
}
