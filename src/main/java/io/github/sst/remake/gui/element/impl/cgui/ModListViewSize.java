package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.interfaces.IWidthSetter;
import net.minecraft.client.MinecraftClient;

public class ModListViewSize implements IWidthSetter {
    @Override
    public void setWidth(CustomGuiScreen forScreen, CustomGuiScreen fromWidthOfThisScreen) {
        forScreen.setX(0);
        if (fromWidthOfThisScreen == null) {
            forScreen.setWidth(MinecraftClient.getInstance().getWindow().getWidth());
        } else {
            forScreen.setWidth(fromWidthOfThisScreen.getWidth());
        }
    }
}
