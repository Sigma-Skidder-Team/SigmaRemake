package io.github.sst.remake.gui.element.impl.cgui.overlay;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.gui.screen.JelloScreen;
import net.minecraft.client.MinecraftClient;

public class BlurOverlay extends AnimatedIconPanel {
    public final JelloScreen field21278;

    public BlurOverlay(JelloScreen var1, CustomGuiScreen var2, String var3) {
        super(var2, var3, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false);
        this.field21278 = var1;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClick(mouseX, mouseY, mouseButton);
    }
}
