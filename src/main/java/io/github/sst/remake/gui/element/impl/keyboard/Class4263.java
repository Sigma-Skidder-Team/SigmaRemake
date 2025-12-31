package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class Class4263 extends Element {
    public float field20678;

    public Class4263(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.field20678 = this.field20678 + (!this.method13298() ? -0.14F : 0.14F);
        this.field20678 = Math.min(Math.max(0.0F, this.field20678), 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                (float) this.width,
                ColorHelper.applyAlpha(ClientColors.PALE_YELLOW.getColor(), (0.5F + this.field20678 * 0.3F + (!this.field20909 ? 0.0F : 0.2F)) * partialTicks)
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
