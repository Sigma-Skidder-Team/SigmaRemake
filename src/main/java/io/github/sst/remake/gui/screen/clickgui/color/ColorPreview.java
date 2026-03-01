package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class ColorPreview extends InteractiveWidget {
    public int previewColor;

    public ColorPreview(GuiComponent parent, String name, int x, int y, int width, int height, int initialColor) {
        super(parent, name, x, y, width, height, false);
        this.previewColor = initialColor;
    }

    @Override
    public void draw(float partialTicks) {
        float centerX = (float) this.x + (float) this.width / 2.0F;
        float centerY = (float) this.y + (float) this.width / 2.0F;

        int outerRingColor = ColorHelper.applyAlpha(
                ColorHelper.shiftTowardsOther(this.previewColor, ClientColors.DEEP_TEAL.getColor(), 0.8F),
                partialTicks
        );
        RenderUtils.drawCircle(centerX, centerY, (float) this.width, outerRingColor);

        RenderUtils.drawCircle(
                centerX,
                centerY,
                (float) (this.width - 2),
                ColorHelper.applyAlpha(this.previewColor, partialTicks)
        );

        if (this.isMouseDownOverComponent()) {
            RenderUtils.drawCircle(
                    centerX,
                    centerY,
                    (float) (this.width - 2),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.2F)
            );
        }

        super.draw(partialTicks);
    }
}