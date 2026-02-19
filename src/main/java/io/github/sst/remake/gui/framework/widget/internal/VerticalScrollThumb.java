package io.github.sst.remake.gui.framework.widget.internal;

import io.github.sst.remake.gui.framework.widget.VerticalScrollBar;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollThumb extends Widget {
    private final ScrollablePanel scrollPanel;
    public final VerticalScrollBar scrollBar;

    public VerticalScrollThumb(VerticalScrollBar var1, VerticalScrollBar var2, int var3) {
        super(var2, "verticalScrollBarButton", 0, 0, var3, 10, true);
        this.scrollBar = var1;
        this.enableImmediateDrag = true;
        this.scrollPanel = (ScrollablePanel) var2.getParent();
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        float var5 = (float) this.scrollPanel.getContent().getHeight();
        float var6 = (float) this.parent.getParent().getHeight();
        float var7 = (float) this.parent.getHeight();
        float var8 = var6 / var5;
        float var9 = var7 * var8;
        float var10 = 20.0F;
        if (!(var9 < var10)) {
            if (var9 > var7) {
                var9 = var7;
            }
        } else {
            var9 = var10;
        }

        this.setHeight((int) var9);
        if (!this.dragging && this.getHeight() != this.parent.getHeight()) {
            if (this.scrollBar.scrollOffset >= 0) {
                if (this.scrollBar.scrollOffset + this.parent.getParent().getHeight() > this.scrollPanel.getContent().getHeight()) {
                    this.scrollBar.scrollOffset = this.scrollPanel.getContent().getHeight() - this.parent.getParent().getHeight();
                }
            } else {
                this.scrollBar.scrollOffset = 0;
            }

            float var16 = var5 - var6;
            float var13 = (float) this.scrollBar.scrollOffset / var16;
            float var14 = (float) (this.parent.getHeight() - this.getHeight());
            float var15 = var14 * var13 + 0.5F;
            this.setY((int) var15);
        } else if (this.isDragging()) {
            float var12 = (float) this.getY() / (float) this.parent.getHeight();
            this.scrollBar.scrollOffset = (int) (var12 * (float) this.scrollPanel.getContent().getHeight());
            if (this.scrollBar.scrollOffset >= 0) {
                if (this.scrollBar.scrollOffset + this.parent.getParent().getHeight() > this.scrollPanel.getContent().getHeight()) {
                    this.scrollBar.scrollOffset = this.scrollPanel.getContent().getHeight() - this.parent.getParent().getHeight();
                }
            } else {
                this.scrollBar.scrollOffset = 0;
            }

            this.scrollBar.scrollActivityTimer.reset();
            this.scrollBar.scrollActivityTimer.start();
        }
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = 5;
        partialTicks *= !this.dragging ? (!this.isHoveredInHierarchy ? 0.3F : 0.7F) : 0.75F;
        int var5 = this.x;
        int var6 = this.width;

        RenderUtils.drawImage((float) var5, (float) this.y, (float) var6, (float) var4, Resources.VERTICAL_SCROLL_BAR_TOP, partialTicks);
        RenderUtils.drawImage((float) var5, (float) (this.y + this.height - var4), (float) var6, (float) var4, Resources.VERTICAL_SCROLL_BAR_BOTTOM, partialTicks);
        RenderUtils.drawRoundedRect(
                (float) var5,
                (float) (this.y + var4),
                (float) (var5 + var6),
                (float) (this.y + this.getHeight() - var4),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * partialTicks)
        );

        super.draw(partialTicks);
    }
}
