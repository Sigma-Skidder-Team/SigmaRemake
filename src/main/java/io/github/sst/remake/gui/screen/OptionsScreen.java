package io.github.sst.remake.gui.screen;

import io.github.sst.remake.gui.screen.holder.OptionsHolder;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class OptionsScreen extends GameMenuScreen {
    public OptionsScreen() {
        super(true);
    }

    @Override
    public void init() {
        addButton(new ButtonWidget(
                width / 2 - 102,
                height - 45,
                204, 20,
                Text.literal("Jello for Fabric Options"),
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
    public boolean shouldPause() {
        return true;
    }
}
