package io.github.sst.remake.gui.element.impl.cgui.block;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Widget;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.item.ItemStack;

public class BlockButton extends Widget {
    public ItemStack stack;
    public boolean field21367;

    public BlockButton(CustomGuiScreen parent, String var2, int var3, int var4, int var5, int var6, ItemStack var7) {
        super(parent, var2, var3, var4, var5, var6, false);
        this.stack = var7;
    }

    @Override
    public void draw(float partialTicks) {
        byte var4 = 5;
        if (this.method13700() || this.isHoveredInHierarchy()) {
            RenderUtils.drawPanelShadow(
                    (float) this.x,
                    (float) this.y,
                    (float) this.width,
                    (float) this.height,
                    14.0F,
                    !this.method13700() ? 0.3F * partialTicks : 0.8F * partialTicks
            );
        }

        RenderUtils.renderItemStack(this.stack, this.x + var4, this.y + var4, this.width - var4 * 2, this.height - var4 * 2);
        super.draw(partialTicks);
    }

    public boolean method13700() {
        return this.field21367;
    }

    public void method13702(boolean var1, boolean var2) {
        if (var1 != this.method13700()) {
            this.field21367 = var1;
            this.callUIHandlers();
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.method13702(!this.field21367, true);
    }
}
