package io.github.sst.remake.gui.element.impl.cgui.color;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.awt.*;

public class ColorPicker extends Widget {
    public int field20618;
    public boolean field20619;
    public ColorPickerBlock field20620;
    public ColorPickerSlider field20621;
    public ColorPickerBubble field20622;

    public ColorPicker(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20618 = var7;
        Color var11 = new Color(var7);
        float[] var12 = Color.RGBtoHSB(var11.getRed(), var11.getGreen(), var11.getBlue(), null);
        this.addToList(this.field20620 = new ColorPickerBlock(this, "block", 10, 10, var5 - 20, var6 - 50, var12[0], var12[1], var12[2]));
        this.addToList(this.field20621 = new ColorPickerSlider(this, "slider", 14, var6 - 25, var5 - 65, 8, var12[0]));
        this.addToList(this.field20622 = new ColorPickerBubble(this, "bubble", var5 - 40, var6 - 32, 25, 25, var11.getRGB()));
        this.field20620.onPress(var1x -> this.method13050());
        this.field20621.onPress(var1x -> this.method13050());
        this.field20622.onClick((var1x, var2x) -> this.method13045(!this.method13047()));
        this.field20619 = var8;
    }

    public void method13045(boolean var1) {
        this.method13046(var1);
        this.callUIHandlers();
    }

    public void method13046(boolean var1) {
        this.field20619 = var1;
    }

    public boolean method13047() {
        return this.field20619;
    }

    public void method13048(int var1) {
        if (var1 != this.field20618) {
            Color var4 = new Color(var1);
            float[] var5 = Color.RGBtoHSB(var4.getRed(), var4.getGreen(), var4.getBlue(), null);
            this.field20620.method13678(var5[0]);
            this.field20620.method13681(var5[1], false);
            this.field20620.method13684(var5[2], false);
            this.field20621.method13098(var5[0], false);
            this.field20622.field21365 = var1;
        }
    }

    public int method13049() {
        return this.field20618;
    }

    private void method13050() {
        this.method13051();
        this.callUIHandlers();
    }

    private void method13051() {
        float var3 = this.field20621.method13096();
        this.field20620.method13678(var3);
        this.field20618 = this.field20620.method13685();
        this.field20622.field21365 = this.field20618;
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
        if (this.field20619) {
            this.field20621.method13098((float) (System.currentTimeMillis() % 4000L) / 4000.0F, false);
            this.method13051();
        }

        super.draw(partialTicks);
    }
}
