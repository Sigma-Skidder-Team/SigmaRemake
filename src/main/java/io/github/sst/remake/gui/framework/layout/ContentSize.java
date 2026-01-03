package io.github.sst.remake.gui.framework.layout;

import io.github.sst.remake.gui.GuiComponent;

public class ContentSize implements WidthSetter {
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
