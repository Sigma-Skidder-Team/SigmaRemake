package io.github.sst.remake.gui.screen.clickgui.slider;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class Slider extends InteractiveWidget {
    private float field20732;
    private float field20733;
    private SliderButton field20734;
    private AnimationUtils field20735;

    public static float method13134(float var0, float var1, float var2) {
        return (var2 - var0) / (var1 - var0);
    }

    public static float method13135(float var0, float var1, float var2, float var3, int var4) {
        float var7 = Math.abs(var2 - var1) / var3;
        float var8 = var1 + var0 * var7 * var3;
        return (float) Math.round((double) var8 * Math.pow(10.0, var4)) / (float) Math.pow(10.0, var4);
    }

    public Slider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.method13136();
    }

    public Slider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, var2, var3, var4, var5, var6, var7, false);
        this.method13136();
    }

    public Slider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, false);
        this.method13136();
    }

    public Slider(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
        this.method13136();
    }

    private void method13136() {
        this.addToList(this.field20734 = new SliderButton(this, this.getHeight()));
        this.field20732 = -1.0F;
        this.field20735 = new AnimationUtils(114, 114, AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.field20735
                .changeDirection(
                        !this.isHoveredInHierarchy() && !this.field20734.isHoveredInHierarchy() && !this.isMouseDownOverComponent() && !this.field20734.isDragging()
                                ? AnimationUtils.Direction.FORWARDS
                                : AnimationUtils.Direction.BACKWARDS
                );
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int var6 = this.getHeight() / 4;
        int var7 = this.getWidth() - this.field20734.getWidth() / 2 - 3;
        int var8 = this.getX() + this.field20734.getWidth() / 4 + 3;
        int var9 = this.getY() + this.getHeight() / 2 - var6 / 2;
        int var10 = this.field20734.getX() + this.field20734.getWidth() / 2 - 6;
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
            int var11 = Math.max(0, 9 - this.field20734.getX());
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) (var8 - FontUtils.HELVETICA_LIGHT_14.getWidth(this.getText()) - 10 - var11),
                    (float) (var9 - 5),
                    this.getText(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F * this.field20735.calcPercent() * partialTicks)
            );
        }

        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            this.field20734.setDragging(true);
            return false;
        } else {
            return true;
        }
    }

    public SliderButton method13137() {
        return this.field20734;
    }

    public float method13138() {
        return this.field20733;
    }

    public void method13139(float var1) {
        this.method13140(var1, true);
    }

    public void method13140(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.field20733;
        this.field20733 = var1;
        this.field20734.setX((int) ((float) (this.getWidth() - this.field20734.getWidth()) * var1 + 0.5F));
        if (var2 && var5 != var1) {
            this.callUIHandlers();
        }
    }

    public boolean method13141() {
        return this.field20732 >= 0.0F && this.field20732 <= 1.0F;
    }

    public float method13142() {
        return this.field20732;
    }

    public void method13143(float var1) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        this.field20732 = var1;
    }
}
