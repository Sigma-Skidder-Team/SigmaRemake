package io.github.sst.remake.gui;

import io.github.sst.remake.gui.interfaces.IWidthSetter;

public class ContentSize implements IWidthSetter {
    @Override
    public void setWidth(CustomGuiScreen of, CustomGuiScreen to) {
        int width = 0;
        int height = 0;

        for (CustomGuiScreen child : of.getChildren()) {
            if (child.getXA() + child.getWidthA() > width) {
                width = child.getXA() + child.getWidthA();
            }

            if (child.getYA() + child.getHeightA() > height) {
                height = child.getYA() + child.getHeightA();
            }
        }

        of.setWidthA(width);
        of.setHeightA(height);
    }
}
