package io.github.sst.remake.gui.element.impl;

import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollBarButton extends AnimatedIconPanel {
    private final ScrollableContentPanel field20780;
    public final VerticalScrollBar field20781;

    public VerticalScrollBarButton(VerticalScrollBar var1, VerticalScrollBar var2, int var3) {
        super(var2, "verticalScrollBarButton", 0, 0, var3, 10, true);
        this.field20781 = var1;
        this.field20886 = true;
        this.field20780 = (ScrollableContentPanel) var2.getParent();
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        float var5 = (float) this.field20780.getButton().getHeightA();
        float var6 = (float) this.parent.getParent().getHeightA();
        float var7 = (float) this.parent.getHeightA();
        float var8 = var6 / var5;
        float var9 = var7 * var8;
        float var10 = 20.0F;
        if (!(var9 < 20.0F)) {
            if (var9 > var7) {
                var9 = var7;
            }
        } else {
            var9 = 20.0F;
        }

        this.setHeightA((int) var9);
        if (!this.field20877 && this.getHeightA() != this.parent.getHeightA()) {
            if (this.field20781.offset >= 0) {
                if (this.field20781.offset + this.parent.getParent().getHeightA() > this.field20780.getButton().getHeightA()) {
                    this.field20781.offset = this.field20780.getButton().getHeightA() - this.parent.getParent().getHeightA();
                }
            } else {
                this.field20781.offset = 0;
            }

            float var16 = var5 - var6;
            float var13 = (float) this.field20781.offset / var16;
            float var14 = (float) (this.parent.getHeightA() - this.getHeightA());
            float var15 = var14 * var13 + 0.5F;
            this.setYA((int) var15);
        } else if (this.isDragging()) {
            float var12 = (float) this.getYA() / (float) this.parent.getHeightA();
            this.field20781.offset = (int) (var12 * (float) this.field20780.getButton().getHeightA());
            if (this.field20781.offset >= 0) {
                if (this.field20781.offset + this.parent.getParent().getHeightA() > this.field20780.getButton().getHeightA()) {
                    this.field20781.offset = this.field20780.getButton().getHeightA() - this.parent.getParent().getHeightA();
                }
            } else {
                this.field20781.offset = 0;
            }

            this.field20781.field20797.reset();
            this.field20781.field20797.start();
        }
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = 5;
        partialTicks *= !this.field20877 ? (!this.field20908 ? 0.3F : 0.7F) : 0.75F;
        int var5 = this.xA;
        int var6 = this.widthA;

        RenderUtils.drawImage((float) var5, (float) this.yA, (float) var6, (float) var4, Resources.verticalScrollBarTopPNG, partialTicks);
        RenderUtils.drawImage((float) var5, (float) (this.yA + this.heightA - var4), (float) var6, (float) var4, Resources.verticalScrollBarBottomPNG, partialTicks);
        RenderUtils.drawRoundedRect(
                (float) var5,
                (float) (this.yA + var4),
                (float) (var5 + var6),
                (float) (this.yA + this.getHeightA() - var4),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * partialTicks)
        );

        super.draw(partialTicks);
    }
}
