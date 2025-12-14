package io.github.sst.remake.gui.element.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.gui.interfaces.Class4293;
import io.github.sst.remake.util.io.GsonUtils;
import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class VerticalScrollBar extends AnimatedIconPanel implements Class4293 {
    public int offset;
    public float field20794;
    public boolean field20795;
    public final VerticalScrollBarButton field20796;
    public TimerUtils field20797 = new TimerUtils();

    public VerticalScrollBar(CustomGuiScreen var1, int var2) {
        super(var1, "verticalScrollBar", var1.getWidthA() - var2 - 5, 5, var2, var1.getHeightA() - 10, false);
        this.setSize((var1x, var2x) -> {
            var1x.setXA(var2x.getWidthA() - var2 - 5);
            var1x.setYA(5);
            var1x.setWidthA(var2);
            var1x.setHeightA(var2x.getHeightA() - 10);
        });
        this.addToList(this.field20796 = new VerticalScrollBarButton(this, this, var2));
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.parent != null && this.parent.method13228(this.getHeightO(), this.getWidthO(), false) || ((ScrollableContentPanel) this.parent).field21208) {
            float var4 = (float) ((ScrollableContentPanel) this.getParent()).getButton().getHeightA();
            float var5 = (float) this.getParent().getHeightA();
            if (var4 == 0.0F) {
                return;
            }

            float var7 = var5 / var4;
            if (var7 >= 1.0F) {
                return;
            }

            this.offset = this.offset
                    - Math.round(!(scroll < 0.0F) ? (float) ((ScrollableContentPanel) this.parent).field21207 * scroll : 1.0F * (float) ((ScrollableContentPanel) this.parent).field21207 * scroll);
            this.field20797.reset();
            this.field20797.start();
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        this.field20908 = this.method13228(newHeight, newWidth, false);
        this.field20794 = this.field20794
                + (
                this.field20796.getHeightA() >= this.getHeightA()
                        ? -1.0F
                        : (
                        !this.method13298() && !this.field20796.method13216() && (!this.field20797.isEnabled() || this.field20797.getElapsedTime() >= 500L)
                                ? -0.05F
                                : 0.05F
                )
        );
        this.field20794 = Math.min(Math.max(0.0F, this.field20794), 1.0F);
        float var5 = (float) ((ScrollableContentPanel) this.getParent()).getButton().getHeightA();
        float var6 = (float) this.getParent().getHeightA();
        float var7 = (float) this.getHeightA();
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
        int var6 = this.xA;
        int var7 = this.widthA;

        RenderUtils.drawImage((float) var6, (float) this.yA, (float) var7, 5.0F, Resources.verticalScrollBarTopPNG, 0.45F * partialTicks);
        RenderUtils.drawImage((float) var6, (float) (this.yA + this.heightA - var4), (float) var7, 5.0F, Resources.verticalScrollBarBottomPNG, 0.45F * partialTicks);
        RenderUtils.drawRoundedRect((float) var6, (float) (this.yA + var4), (float) (var6 + var7), (float) (this.yA + this.heightA - var4), var5);


        super.draw(partialTicks);
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onClick(mouseX, mouseY, mouseButton)) {
            this.field20908 = this.method13228(mouseX, mouseY, false);
            if (this.method13298()) {
                int var6 = mouseY - this.method13272();
                if (var6 <= this.field20796.getYA() + this.field20796.getHeightA()) {
                    if (var6 < this.field20796.getYA()) {
                        this.offset = this.offset - (int) ((float) ((ScrollableContentPanel) this.parent).getButton().getHeightA() / 4.0F);
                    }
                } else {
                    this.offset = this.offset + (int) ((float) ((ScrollableContentPanel) this.parent).getButton().getHeightA() / 4.0F);
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
    public int method13162() {
        return this.offset;
    }

}
