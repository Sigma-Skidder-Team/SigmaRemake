package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.interfaces.WidthSetter;
import net.minecraft.client.MinecraftClient;

public class ModListViewSize implements WidthSetter {
    @Override
    public void setWidth(GuiComponent forScreen, GuiComponent fromWidthOfThisScreen) {
        forScreen.setX(0);
        if (fromWidthOfThisScreen == null) {
            forScreen.setWidth(MinecraftClient.getInstance().getWindow().getWidth());
        } else {
            forScreen.setWidth(fromWidthOfThisScreen.getWidth());
        }
    }
}
