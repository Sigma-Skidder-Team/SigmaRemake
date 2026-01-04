package io.github.sst.remake.gui.framework.widget;

import com.google.gson.JsonObject;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.internal.VerticalScrollBarButton;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.layout.OffsetProvider;
import io.github.sst.remake.util.io.GsonUtils;
import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollBar extends Widget implements OffsetProvider {
    public int offset;
    public float field20794;
    public final VerticalScrollBarButton field20796;
    public TimerUtils field20797 = new TimerUtils();

    public VerticalScrollBar(GuiComponent var1, int var2) {
        super(var1, "verticalScrollBar", var1.getWidth() - var2 - 5, 5, var2, var1.getHeight() - 10, false);
        this.addWidthSetter((var1x, var2x) -> {
            var1x.setX(var2x.getWidth() - var2 - 5);
            var1x.setY(5);
            var1x.setWidth(var2);
            var1x.setHeight(var2x.getHeight() - 10);
        });
        this.addToList(this.field20796 = new VerticalScrollBarButton(this, this, var2));
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.parent != null && this.parent.isMouseOverComponentConsideringZOrder(this.getMouseX(), this.getMouseY(), false) || ((ScrollablePanel) this.parent).field21208) {
            float var4 = (float) ((ScrollablePanel) this.getParent()).getButton().getHeight();
            float var5 = (float) this.getParent().getHeight();
            if (var4 == 0.0F) {
                return;
            }

            float var7 = var5 / var4;
            if (var7 >= 1.0F) {
                return;
            }

            this.offset = this.offset
                    - Math.round(!(scroll < 0.0F) ? (float) ((ScrollablePanel) this.parent).field21207 * scroll : 1.0F * (float) ((ScrollablePanel) this.parent).field21207 * scroll);
            this.field20797.reset();
            this.field20797.start();
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.isHoveredInHierarchy = this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, false);
        this.field20794 = this.field20794
                + (
                this.field20796.getHeight() >= this.getHeight()
                        ? -1.0F
                        : (
                        !this.isHoveredInHierarchy() && !this.field20796.isDragging() && (!this.field20797.isEnabled() || this.field20797.getElapsedTime() >= 500L)
                                ? -0.05F
                                : 0.05F
                )
        );
        this.field20794 = Math.min(Math.max(0.0F, this.field20794), 1.0F);
        float var5 = (float) ((ScrollablePanel) this.getParent()).getButton().getHeight();
        float var6 = (float) this.getParent().getHeight();
        float var7 = (float) this.getHeight();
        float var8 = var6 / var5;
        boolean var9 = var8 < 1.0F && var5 > 0.0F && this.field20794 >= 0.0F;
        this.setSelfVisible(var9);
        this.setHovered(var9);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks *= this.field20794;
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
                if (var6 <= this.field20796.getY() + this.field20796.getHeight()) {
                    if (var6 < this.field20796.getY()) {
                        this.offset = this.offset - (int) ((float) ((ScrollablePanel) this.parent).getButton().getHeight() / 4.0F);
                    }
                } else {
                    this.offset = this.offset + (int) ((float) ((ScrollablePanel) this.parent).getButton().getHeight() / 4.0F);
                }
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        config.addProperty("offset", this.offset);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);
        this.offset = GsonUtils.getIntOrDefault(config, "offset", this.offset);
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
