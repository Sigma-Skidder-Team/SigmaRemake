package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.layout.WidthSetter;
import net.minecraft.client.MinecraftClient;

public class ModuleListResizer implements WidthSetter {
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
