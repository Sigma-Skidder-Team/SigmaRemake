package io.github.sst.remake.gui.framework.widget;

import com.google.gson.JsonObject;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.internal.VerticalScrollThumb;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.layout.OffsetProvider;
import io.github.sst.remake.util.io.GsonUtils;
import io.github.sst.remake.util.math.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollBar extends Widget implements OffsetProvider {
    public int scrollOffset;
    public float fadeAlpha;
    private final VerticalScrollThumb thumb;
    public TogglableTimer scrollActivityTimer = new TogglableTimer();

    public VerticalScrollBar(GuiComponent var1, int var2) {
        super(var1, "verticalScrollBar", var1.getWidth() - var2 - 5, 5, var2, var1.getHeight() - 10, false);
        this.addWidthSetter((var1x, var2x) -> {
            var1x.setX(var2x.getWidth() - var2 - 5);
            var1x.setY(5);
            var1x.setWidth(var2);
            var1x.setHeight(var2x.getHeight() - 10);
        });
        this.addToList(this.thumb = new VerticalScrollThumb(this, this, var2));
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.parent != null && this.parent.isMouseOverComponentConsideringZOrder(this.getMouseX(), this.getMouseY(), false) || ((ScrollablePanel) this.parent).reserveScrollbarSpace) {
            float var4 = (float) ((ScrollablePanel) this.getParent()).getContent().getHeight();
            float var5 = (float) this.getParent().getHeight();
            if (var4 == 0.0F) {
                return;
            }

            float var7 = var5 / var4;
            if (var7 >= 1.0F) {
                return;
            }

            this.scrollOffset = this.scrollOffset
                    - Math.round(!(scroll < 0.0F) ? (float) ((ScrollablePanel) this.parent).scrollBarWidth * scroll : 1.0F * (float) ((ScrollablePanel) this.parent).scrollBarWidth * scroll);
            this.scrollActivityTimer.reset();
            this.scrollActivityTimer.start();
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.isHoveredInHierarchy = this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, false);
        this.fadeAlpha = this.fadeAlpha
                + (
                this.thumb.getHeight() >= this.getHeight()
                        ? -1.0F
                        : (
                        !this.isHoveredInHierarchy() && !this.thumb.isDragging() && (!this.scrollActivityTimer.isEnabled() || this.scrollActivityTimer.getElapsedTime() >= 500L)
                                ? -0.05F
                                : 0.05F
                )
        );
        this.fadeAlpha = Math.min(Math.max(0.0F, this.fadeAlpha), 1.0F);
        float var5 = (float) ((ScrollablePanel) this.getParent()).getContent().getHeight();
        float var6 = (float) this.getParent().getHeight();
        float var8 = var6 / var5;
        boolean var9 = var8 < 1.0F && var5 > 0.0F && this.fadeAlpha >= 0.0F;
        this.setSelfVisible(var9);
        this.setHovered(var9);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks *= this.fadeAlpha;
        int var4 = 5;
        int var5 = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.2F * partialTicks);
        int var6 = this.x;
        int var7 = this.width;

        RenderUtils.drawImage((float) var6, (float) this.y, (float) var7, 5.0F, Resources.VERTICAL_SCROLL_BAR_TOP, 0.45F * partialTicks);
        RenderUtils.drawImage((float) var6, (float) (this.y + this.height - var4), (float) var7, 5.0F, Resources.VERTICAL_SCROLL_BAR_BOTTOM, 0.45F * partialTicks);
        RenderUtils.drawRoundedRect((float) var6, (float) (this.y + var4), (float) (var6 + var7), (float) (this.y + this.height - var4), var5);


        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            this.isHoveredInHierarchy = this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, false);
            if (this.isHoveredInHierarchy()) {
                int var6 = mouseY - this.getAbsoluteY();
                if (var6 <= this.thumb.getY() + this.thumb.getHeight()) {
                    if (var6 < this.thumb.getY()) {
                        this.scrollOffset = this.scrollOffset - (int) ((float) ((ScrollablePanel) this.parent).getContent().getHeight() / 4.0F);
                    }
                } else {
                    this.scrollOffset = this.scrollOffset + (int) ((float) ((ScrollablePanel) this.parent).getContent().getHeight() / 4.0F);
                }
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        config.addProperty("offset", this.scrollOffset);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);
        this.scrollOffset = GsonUtils.getIntOrDefault(config, "offset", this.scrollOffset);
    }

    @Override
    public int getOffset() {
        return this.scrollOffset;
    }

    @Override
    public void setOffset(int offset) {
        this.scrollOffset = offset;
    }
}
