package io.github.sst.remake.gui.framework.layout;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import net.minecraft.client.MinecraftClient;

public class FullWidthResizer implements WidthSetter {
    @Override
    public void setWidth(GuiComponent targetComponent, GuiComponent widthReferenceComponent) {
        targetComponent.setX(0);

        int width = (widthReferenceComponent == null)
                ? MinecraftClient.getInstance().getWindow().getWidth()
                : widthReferenceComponent.getWidth();

        targetComponent.setWidth(width);
    }
}