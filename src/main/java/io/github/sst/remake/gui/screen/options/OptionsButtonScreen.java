package io.github.sst.remake.gui.screen.options;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.screen.holder.OptionsHolder;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class OptionsButtonScreen extends GameMenuScreen {
    private static long lastSaveTime;

    public OptionsButtonScreen() {
        super(true);

        long now = System.currentTimeMillis();
        if (now - lastSaveTime >= 3000L) {
            lastSaveTime = now;

            Client.LOGGER.info("Saving profiles...");

            Client.INSTANCE.configManager.saveProfile(Client.INSTANCE.configManager.currentProfile, false);
            Client.INSTANCE.configManager.saveScreenConfig(true);
            Client.INSTANCE.configManager.saveClientConfig();
        }
    }

    @Override
    public void init() {
        addButton(new ButtonWidget(
                width / 2 - 102,
                height - 45,
                204, 20,
                new LiteralText("Jello for Fabric Options"),
                w -> client.openScreen(new OptionsHolder())
        ));
        super.init();

        this.buttons.removeIf(widget -> widget.y == this.height / 4 + 72 - 16);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
