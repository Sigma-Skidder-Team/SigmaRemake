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

    public VerticalScrollThumb(VerticalScrollBar scrollBar, VerticalScrollBar parentBar, int width) {
        super(parentBar, "verticalScrollBarButton", 0, 0, width, 10, true);
        this.scrollBar = scrollBar;
        this.enableImmediateDrag = true;
        this.scrollPanel = (ScrollablePanel) parentBar.getParent();
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        float contentHeight = (float) this.scrollPanel.getContent().getHeight();
        float viewHeight = (float) this.parent.getParent().getHeight();
        float trackHeight = (float) this.parent.getHeight();

        float heightRatio = viewHeight / contentHeight;
        float calculatedThumbHeight = trackHeight * heightRatio;
        float minThumbHeight = 20.0F;

        if (calculatedThumbHeight < minThumbHeight) {
            calculatedThumbHeight = minThumbHeight;
        } else if (calculatedThumbHeight > trackHeight) {
            calculatedThumbHeight = trackHeight;
        }

        this.setHeight((int) calculatedThumbHeight);

        if (!this.dragging && this.getHeight() != this.parent.getHeight()) {
            if (this.scrollBar.scrollOffset >= 0) {
                if (this.scrollBar.scrollOffset + viewHeight > contentHeight) {
                    this.scrollBar.scrollOffset = (int) (contentHeight - viewHeight);
                }
            } else {
                this.scrollBar.scrollOffset = 0;
            }

            float scrollableRange = contentHeight - viewHeight;
            float scrollProgress = (float) this.scrollBar.scrollOffset / scrollableRange;
            float thumbTrackRange = (float) (this.parent.getHeight() - this.getHeight());

            this.setY((int) (thumbTrackRange * scrollProgress + 0.5F));
        } else if (this.isDragging()) {
            float dragRatio = (float) this.getY() / (float) this.parent.getHeight();
            this.scrollBar.scrollOffset = (int) (dragRatio * contentHeight);

            if (this.scrollBar.scrollOffset >= 0) {
                if (this.scrollBar.scrollOffset + viewHeight > contentHeight) {
                    this.scrollBar.scrollOffset = (int) (contentHeight - viewHeight);
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
        int texturePadding = 5;
        float alphaMult = !this.dragging ? (!this.isHoveredInHierarchy ? 0.3F : 0.7F) : 0.75F;
        float renderAlpha = partialTicks * alphaMult;

        RenderUtils.drawImage((float) this.x, (float) this.y, (float) this.width, (float) texturePadding, Resources.VERTICAL_SCROLL_BAR_TOP, renderAlpha);
        RenderUtils.drawImage((float) this.x, (float) (this.y + this.height - texturePadding), (float) this.width, (float) texturePadding, Resources.VERTICAL_SCROLL_BAR_BOTTOM, renderAlpha);

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) (this.y + texturePadding),
                (float) (this.x + this.width),
                (float) (this.y + this.getHeight() - texturePadding),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * renderAlpha)
        );

        super.draw(partialTicks);
    }
}
