package io.github.sst.remake.gui.framework.widget;

import com.google.gson.JsonObject;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.internal.VerticalScrollThumb;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.layout.OffsetProvider;
import io.github.sst.remake.util.system.io.GsonUtils;
import io.github.sst.remake.util.math.timer.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollBar extends Widget implements OffsetProvider {
    public int scrollOffset;
    public float fadeAlpha;
    private final VerticalScrollThumb thumb;
    public TogglableTimer scrollActivityTimer = new TogglableTimer();

    public VerticalScrollBar(GuiComponent parent, int width) {
        super(parent, "verticalScrollBar", parent.getWidth() - width - 5, 5, width, parent.getHeight() - 10, false);

        this.addWidthSetter((widget, p) -> {
            widget.setX(p.getWidth() - width - 5);
            widget.setY(5);
            widget.setWidth(width);
            widget.setHeight(p.getHeight() - 10);
        });

        this.addToList(this.thumb = new VerticalScrollThumb(this, this, width));
    }

    @Override
    public void onScroll(float scrollDelta) {
        super.onScroll(scrollDelta);
        ScrollablePanel panel = (ScrollablePanel) this.parent;

        if (panel != null && (panel.isMouseOverComponentConsideringZOrder(this.getMouseX(), this.getMouseY(), false) || panel.reserveScrollbarSpace)) {
            float contentHeight = (float) panel.getContent().getHeight();
            float viewHeight = (float) this.getParent().getHeight();

            if (contentHeight == 0.0F) return;

            float ratio = viewHeight / contentHeight;
            if (ratio >= 1.0F) return;

            float multiplier = (scrollDelta < 0.0F) ? scrollDelta : 1.0F * scrollDelta;
            this.scrollOffset -= Math.round(panel.scrollBarWidth * multiplier);

            this.scrollActivityTimer.reset();
            this.scrollActivityTimer.start();
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.isHoveredInHierarchy = this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, false);

        boolean isTimerExpired = !this.scrollActivityTimer.isEnabled() || this.scrollActivityTimer.getElapsedTime() >= 500L;
        float fadeDirection = (this.thumb.getHeight() >= this.getHeight()) ? -1.0F :
                (!this.isHoveredInHierarchy() && !this.thumb.isDragging() && isTimerExpired ? -0.05F : 0.05F);

        this.fadeAlpha = Math.min(Math.max(0.0F, this.fadeAlpha + fadeDirection), 1.0F);

        float contentHeight = (float) ((ScrollablePanel) this.getParent()).getContent().getHeight();
        float viewHeight = (float) this.getParent().getHeight();
        boolean isVisible = (viewHeight / contentHeight) < 1.0F && contentHeight > 0.0F && this.fadeAlpha >= 0.0F;

        this.setSelfVisible(isVisible);
        this.setHovered(isVisible);
    }

    @Override
    public void draw(float partialTicks) {
        float effectiveAlpha = partialTicks * this.fadeAlpha;
        int padding = 5;
        int trackColor = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.2F * effectiveAlpha);

        RenderUtils.drawImage((float) this.x, (float) this.y, (float) this.width, 5.0F, Resources.VERTICAL_SCROLL_BAR_TOP, 0.45F * effectiveAlpha);
        RenderUtils.drawImage((float) this.x, (float) (this.y + this.height - padding), (float) this.width, 5.0F, Resources.VERTICAL_SCROLL_BAR_BOTTOM, 0.45F * effectiveAlpha);
        RenderUtils.drawRoundedRect((float) this.x, (float) (this.y + padding), (float) (this.x + this.width), (float) (this.y + this.height - padding), trackColor);

        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            this.isHoveredInHierarchy = this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, false);
            if (this.isHoveredInHierarchy()) {
                int localY = mouseY - this.getAbsoluteY();
                int scrollAmount = (int) ((float) ((ScrollablePanel) this.parent).getContent().getHeight() / 4.0F);

                if (localY <= this.thumb.getY() + this.thumb.getHeight()) {
                    if (localY < this.thumb.getY()) this.scrollOffset -= scrollAmount;
                } else {
                    this.scrollOffset += scrollAmount;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public JsonObject toPersistedConfig(JsonObject config) {
        config.addProperty("offset", this.scrollOffset);
        return super.toPersistedConfig(config);
    }

    @Override
    public void loadPersistedConfig(JsonObject config) {
        super.loadPersistedConfig(config);
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
