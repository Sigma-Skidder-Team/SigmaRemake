package io.github.sst.remake.gui.screen;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

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
                new LiteralText("Jello for Fabric Options"),
                w -> client.openScreen(new OptionsScreen())
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
