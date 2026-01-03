package io.github.sst.remake.gui.screen.clickgui.config;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.panel.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class EditButton extends Widget {
    public final int field20768;

    public EditButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20768 = var5;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.getWidth() != 0) {
            this.applyTranslationTransforms();
            float var4 = 1.0F - Math.min(1.0F, Math.max((float) this.getWidth() / (float) this.field20768, 0.0F));
            RenderUtils.drawRoundedRect2(
                    (float) this.x, (float) this.y, (float) this.field20768, (float) this.height, ColorHelper.applyAlpha(-3254955, partialTicks)
            );
            super.draw(partialTicks * (1.0F - var4));
            RenderUtils.drawImage(
                    0.0F, 0.0F, 20.0F, (float) this.height, Resources.shadowRightPNG, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks)
            );
        }
    }
}
