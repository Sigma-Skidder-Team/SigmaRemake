package io.github.sst.remake.gui.screen.clickgui.block;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.item.ItemStack;

public class BlockItemButton extends InteractiveWidget {
    public ItemStack itemStack;
    public boolean selected;

    public BlockItemButton(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ItemStack itemStack
    ) {
        super(parent, name, x, y, width, height, false);
        this.itemStack = itemStack;
    }

    @Override
    public void draw(float partialTicks) {
        final int padding = 5;

        if (this.isSelected() || this.isHoveredInHierarchy()) {
            float shadowAlpha = this.isSelected()
                    ? 0.8F * partialTicks
                    : 0.3F * partialTicks;

            RenderUtils.drawPanelShadow(
                    (float) this.x,
                    (float) this.y,
                    (float) this.width,
                    (float) this.height,
                    14.0F,
                    shadowAlpha
            );
        }

        RenderUtils.renderItemStack(
                this.itemStack,
                this.x + padding,
                this.y + padding,
                this.width - padding * 2,
                this.height - padding * 2
        );

        super.draw(partialTicks);
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected, boolean notify) {
        if (selected != this.isSelected()) {
            this.selected = selected;

            if (notify) {
                this.firePressHandlers();
            }
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.setSelected(!this.selected, true);
    }
}