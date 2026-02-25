package io.github.sst.remake.gui.screen.clickgui.slider;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class SettingSlider extends InteractiveWidget {
    private float snapValue;
    private float value;
    private SliderHandle handle;
    private AnimationUtils labelFade;

    public static float normalizeValue(float var0, float var1, float var2) {
        return (var2 - var0) / (var1 - var0);
    }

    public static float denormalizeValue(float var0, float var1, float var2, float var3, int var4) {
        float var7 = Math.abs(var2 - var1) / var3;
        float var8 = var1 + var0 * var7 * var3;
        return (float) Math.round((double) var8 * Math.pow(10.0, var4)) / (float) Math.pow(10.0, var4);
    }

    public SettingSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.initHandle();
    }

    public SettingSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, var2, var3, var4, var5, var6, var7, false);
        this.initHandle();
    }

    public SettingSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, false);
        this.initHandle();
    }

    public SettingSlider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
        this.initHandle();
    }

    private void initHandle() {
        this.addToList(this.handle = new SliderHandle(this, this.getHeight()));
        this.snapValue = -1.0F;
        this.labelFade = new AnimationUtils(114, 114, AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.labelFade
                .changeDirection(
                        !this.isHoveredInHierarchy() && !this.handle.isHoveredInHierarchy() && !this.isMouseDownOverComponent() && !this.handle.isDragging()
                                ? AnimationUtils.Direction.FORWARDS
                                : AnimationUtils.Direction.BACKWARDS
                );
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int var6 = this.getHeight() / 4;
        int var7 = this.getWidth() - this.handle.getWidth() / 2 - 3;
        int var8 = this.getX() + this.handle.getWidth() / 4 + 3;
        int var9 = this.getY() + this.getHeight() / 2 - var6 / 2;
        int var10 = this.handle.getX() + this.handle.getWidth() / 2 - 6;
        RenderUtils.drawRoundedRect(
                (float) var8, (float) var9, (float) var10, (float) var6, (float) (var6 / 2), ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * partialTicks * partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (var8 + var10),
                (float) var9,
                (float) (var7 - var10),
                (float) var6,
                (float) (var6 / 2),
                ColorHelper.applyAlpha(ColorHelper.adjustColorTowardsWhite(this.textColor.getPrimaryColor(), 0.8F), partialTicks * partialTicks * partialTicks)
        );
        if (this.getText() != null) {
            int var11 = Math.max(0, 9 - this.handle.getX());
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) (var8 - FontUtils.HELVETICA_LIGHT_14.getWidth(this.getText()) - 10 - var11),
                    (float) (var9 - 5),
                    this.getText(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F * this.labelFade.calcPercent() * partialTicks)
            );
        }

        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            this.handle.setDragging(true);
            return false;
        } else {
            return true;
        }
    }

    public SliderHandle getHandle() {
        return this.handle;
    }

    public float getValue() {
        return this.value;
    }

    public void setValueFromHandle(float var1) {
        this.setValue(var1, true);
    }

    public void setValue(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.value;
        this.value = var1;
        this.handle.setX((int) ((float) (this.getWidth() - this.handle.getWidth()) * var1 + 0.5F));
        if (var2 && var5 != var1) {
            this.firePressHandlers();
        }
    }

    public boolean hasSnapValue() {
        return this.snapValue >= 0.0F && this.snapValue <= 1.0F;
    }

    public float getSnapValue() {
        return this.snapValue;
    }

    public void setSnapValue(float var1) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        this.snapValue = var1;
    }
}
