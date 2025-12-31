package io.github.sst.remake.gui.impl;

import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.spotlight.SpotlightDialog;
import net.minecraft.client.MinecraftClient;

public class JelloSpotlight extends Screen {
    public SpotlightDialog dialog;

    public JelloSpotlight() {
        super("Spotlight");
        this.setListening(false);
        int x = (this.getWidth() - 675) / 2;
        this.addToList(this.dialog = new SpotlightDialog(this, "search", x, (int) ((float) this.height * 0.25F), 675, 60, true));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}
