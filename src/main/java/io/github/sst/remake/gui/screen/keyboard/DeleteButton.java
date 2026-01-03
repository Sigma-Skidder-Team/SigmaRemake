package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class DeleteButton extends InteractiveWidget {
    public float hoverAnimationProgress;

    public DeleteButton(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height, false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.hoverAnimationProgress = this.hoverAnimationProgress + (!this.isHoveredInHierarchy() ? -0.14F : 0.14F);
        this.hoverAnimationProgress = Math.min(Math.max(0.0F, this.hoverAnimationProgress), 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                (float) this.width,
                ColorHelper.applyAlpha(ClientColors.PALE_YELLOW.getColor(), (0.5F + this.hoverAnimationProgress * 0.3F + (!this.isMouseDownOverComponent ? 0.0F : 0.2F)) * partialTicks)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + (this.width - 10) / 2),
                (float) (this.y + this.height / 2 - 1),
                10.0F,
                2.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.75F * partialTicks)
        );
        super.draw(partialTicks);
    }
}
