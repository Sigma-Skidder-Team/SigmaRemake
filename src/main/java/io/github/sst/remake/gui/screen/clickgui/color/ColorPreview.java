package io.github.sst.remake.gui.screen.clickgui.color;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class ColorPreview extends InteractiveWidget {
    public int colorValue;

    public ColorPreview(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, int var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.colorValue = var7;
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawCircle(
                (float) this.x + (float) this.width / 2.0F,
                (float) this.y + (float) this.width / 2.0F,
                (float) this.width,
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(this.colorValue, ClientColors.DEEP_TEAL.getColor(), 0.8F), partialTicks)
        );
        RenderUtils.drawCircle(
                (float) this.x + (float) this.width / 2.0F,
                (float) this.y + (float) this.width / 2.0F,
                (float) (this.width - 2),
                ColorHelper.applyAlpha(this.colorValue, partialTicks)
        );
        if (this.isMouseDownOverComponent()) {
            RenderUtils.drawCircle(
                    (float) this.x + (float) this.width / 2.0F,
                    (float) this.y + (float) this.width / 2.0F,
                    (float) (this.width - 2),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.2F)
            );
        }

        super.draw(partialTicks);
    }
}
