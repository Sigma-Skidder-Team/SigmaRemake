package io.github.sst.remake.gui.screen.clickgui.config;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.util.math.color.ColorHelper;

public class ProfileActionButton extends Button {
    public ProfileActionButton(
            GuiComponent parent,
            String text,
            int x,
            int y,
            int width,
            int height,
            ColorHelper colors,
            String tooltip
    ) {
        super(parent, text, x, y, width, height, colors, tooltip);
    }

    @Override
    public void draw(float partialTicks) {
        this.getWidthSetters().get(0).setWidth(this, this.parent);

        super.draw(partialTicks);
    }
}