package io.github.sst.remake.gui.element.impl.cgui.color;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;

import java.awt.*;

public class ColorPickerBlock extends Widget {
    private static String[] field20602;
    public float field21347;
    private float field21348 = 0.0F;
    private float field21349 = 1.0F;
    public boolean field21350 = false;

    public ColorPickerBlock(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, float var7, float var8, float var9) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field21347 = var7;
        this.field21348 = var8;
        this.field21349 = var9;
    }

    public void method13678(float var1) {
        this.field21347 = var1;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.field21350) {
            int var5 = this.getMouseX() - this.getAbsoluteX();
            this.method13680((float) var5 / (float) this.getWidth());
            int var6 = this.getMouseY() - this.getAbsoluteY();
            this.method13683(1.0F - (float) var6 / (float) this.getHeight());
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = ColorHelper.applyAlpha(Color.HSBtoRGB(this.field21347, 0.0F, 1.0F), partialTicks);
        int var5 = ColorHelper.applyAlpha(Color.HSBtoRGB(this.field21347, 1.0F, 1.0F), partialTicks);
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
                this.x + Math.round((float) this.width * this.method13679()),
                this.y + Math.round((float) this.height * (1.0F - this.method13682())),
                Color.HSBtoRGB(this.field21347, this.field21348, this.field21349),
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
        this.field21350 = true;
        return super.onMouseDown(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        this.field21350 = false;
    }

    public float method13679() {
        return this.field21348;
    }

    public void method13680(float var1) {
        this.method13681(var1, true);
    }

    public void method13681(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.field21348;
        this.field21348 = var1;
        if (var2 && var5 != var1) {
            this.callUIHandlers();
        }
    }

    public float method13682() {
        return this.field21349;
    }

    public void method13683(float var1) {
        this.method13684(var1, true);
    }

    public void method13684(float var1, boolean var2) {
        var1 = Math.min(Math.max(var1, 0.0F), 1.0F);
        float var5 = this.field21349;
        this.field21349 = var1;
        if (var2 && var5 != var1) {
            this.callUIHandlers();
        }
    }

    public int method13685() {
        return Color.HSBtoRGB(this.field21347, this.field21348, this.field21349);
    }
}
