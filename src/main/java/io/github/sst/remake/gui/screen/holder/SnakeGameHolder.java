package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SnakeGameHolder extends Screen {
    public SnakeGameHolder(Text title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
