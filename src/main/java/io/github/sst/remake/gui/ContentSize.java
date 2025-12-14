package io.github.sst.remake.gui;

import io.github.sst.remake.gui.interfaces.IWidthSetter;

public class ContentSize implements IWidthSetter {
    @Override
    public void setWidth(CustomGuiScreen forScreen, CustomGuiScreen fromWidthOfThisScreen) {
        int var5 = 0;
        int var6 = 0;

        for (CustomGuiScreen var8 : forScreen.getChildren()) {
            if (var8.getXA() + var8.getWidthA() > var5) {
                var5 = var8.getXA() + var8.getWidthA();
            }

            if (var8.getYA() + var8.getHeightA() > var6) {
                var6 = var8.getYA() + var8.getHeightA();
            }
        }

        forScreen.setWidthA(var5);
        forScreen.setHeightA(var6);
    }
}
