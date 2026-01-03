package io.github.sst.remake.gui;

import io.github.sst.remake.gui.interfaces.IWidthSetter;

public class ContentSize implements IWidthSetter {
    @Override
    public void setWidth(GuiComponent of, GuiComponent to) {
        int width = 0;
        int height = 0;

        for (GuiComponent child : of.getChildren()) {
            if (child.getX() + child.getWidth() > width) {
                width = child.getX() + child.getWidth();
            }

            if (child.getY() + child.getHeight() > height) {
                height = child.getY() + child.getHeight();
            }
        }

        of.setWidth(width);
        of.setHeight(height);
    }
}
