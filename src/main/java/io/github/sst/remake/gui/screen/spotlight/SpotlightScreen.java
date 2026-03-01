package io.github.sst.remake.gui.screen.spotlight;

import io.github.sst.remake.gui.framework.core.Screen;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class SpotlightScreen extends Screen {
    public SearchDialog dialog;

    public SpotlightScreen() {
        super("Spotlight");
        this.setListening(false);
        int x = (this.getWidth() - 675) / 2;
        this.addToList(this.dialog = new SearchDialog(this, "search", x, (int) ((float) this.height * 0.25F), 675, 60, true));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}
