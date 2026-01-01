package io.github.sst.remake.gui.element.impl.mainmenu;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.impl.JelloMenu;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class FloatingBubble extends CustomGuiScreen implements IMinecraft {
    public float field20928;
    public float field20929;
    public float field20930;
    public float field20931;
    public float field20932;
    public float field20933;
    public float field20934;
    public int field20936;
    public int field20937;
    public int field20938 = 114;

    public FloatingBubble(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, int var7) {
        super(var1, var2, var3, var4, var5, var5);
        this.field20928 = this.field20930 = (float) var6;
        this.field20929 = this.field20931 = (float) var7;
        this.field20932 = (float) var3;
        this.field20933 = (float) var4;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.field20932 == -9999.0F || this.field20933 == -9999.0F) {
            this.field20932 = (float) this.x;
            this.field20933 = (float) this.y;
        }

        this.field20932 = this.field20932 + this.field20928 * JelloMenu.field20982;
        this.field20933 = this.field20933 + this.field20929 * JelloMenu.field20982;
        this.x = Math.round(this.field20932);
        this.y = Math.round(this.field20933);
        if (!(this.field20932 + (float) this.width < 0.0F)) {
            if (this.field20932 > (float) client.getWindow().getWidth()) {
                this.field20932 = (float) (-this.width);
            }
        } else {
            this.field20932 = (float) client.getWindow().getWidth();
        }

        if (!(this.field20933 + (float) this.height < 0.0F)) {
            if (this.field20933 > (float) client.getWindow().getHeight()) {
                this.field20933 = (float) (-this.height);
            }
        } else {
            this.field20933 = (float) client.getWindow().getHeight();
        }

        float var5 = (float) (mouseX - this.getAbsoluteX());
        float var6 = (float) (mouseY - this.getAbsoluteY());
        this.field20934 = (float) (1.0 - Math.sqrt(var5 * var5 + var6 * var6) / (double) this.field20938);
        if (!(Math.sqrt(var5 * var5 + var6 * var6) < (double) this.field20938)) {
            this.field20928 = this.field20928 - (this.field20928 - this.field20930) * 0.05F * JelloMenu.field20982;
            this.field20929 = this.field20929 - (this.field20929 - this.field20931) * 0.05F * JelloMenu.field20982;
        } else {
            float var7 = this.field20932 - (float) mouseX;
            float var8 = this.field20933 - (float) mouseY;
            float var9 = (float) Math.sqrt(var7 * var7 + var8 * var8);
            float var10 = var9 / 2.0F;
            float var11 = var7 / var10;
            float var12 = var8 / var10;
            this.field20928 = this.field20928 + var11 / (1.0F + this.field20934) * JelloMenu.field20982;
            this.field20929 = this.field20929 + var12 / (1.0F + this.field20934) * JelloMenu.field20982;
        }

        this.field20936 = mouseX;
        this.field20937 = mouseY;
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawFilledArc(
                (float) this.x,
                (float) this.y,
                (float) this.getWidth(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.07F + (!(this.field20934 > 0.0F) ? 0.0F : this.field20934 * 0.3F))
        );
        super.draw(partialTicks);
    }
}
