package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.util.Date;

public class Class4253 extends Element {
    public Class6984 field20624;
    public Date field20625;
    public int field20626;
    public Date field20627;
    public Class4263 field20628;

    public Class4253(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Class6984 var7, int var8) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.addToList(this.field20628 = new Class4263(this, "delete", 200, 20, 20, 20));
        this.field20628.onClick((var1x, var2x) -> {
            this.field20625 = new Date();
            this.callUIHandlers();
        });
        this.field20624 = var7;
        this.field20626 = var8;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void method13056() {
        this.setHeight(0);
        this.field20627 = new Date();
    }

    @Override
    public void draw(float partialTicks) {
        if (this.field20627 != null) {
            float var4 = AnimationUtils.calculateProgress(this.field20627, 150.0F);
            var4 = QuadraticEasing.easeOutQuad(var4, 0.0F, 1.0F, 1.0F);
            this.setHeight((int) (55.0F * var4));
            if (var4 == 1.0F) {
                this.field20627 = null;
            }
        }

        if (this.field20625 != null) {
            float var6 = AnimationUtils.calculateProgress(this.field20625, 180.0F);
            var6 = QuadraticEasing.easeOutQuad(var6, 0.0F, 1.0F, 1.0F);
            this.setHeight((int) (55.0F * (1.0F - var6)));
            if (var6 == 1.0F) {
                this.field20625 = null;
            }
        }

        ScissorUtils.startScissor(this.x, this.y, this.x + this.width, this.y + this.height, true);
        RenderUtils.drawString(
                FontUtils.RegularFont20,
                (float) (this.x + 25),
                (float) this.y + (float) this.height / 2.0F - 17.5F,
                this.field20624.method21596(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.JelloLightFont12,
                (float) (this.x + 25),
                (float) this.y + (float) this.height / 2.0F + 7.5F,
                this.field20624.method21597(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * partialTicks)
        );
        this.field20628.setY((int) ((float) this.height / 2.0F - 7.5F));
        super.draw(partialTicks);
        ScissorUtils.restoreScissor();
    }
}
