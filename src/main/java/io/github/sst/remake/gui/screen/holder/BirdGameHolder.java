package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BirdGameHolder extends Screen {
    public BirdGameHolder(Text title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
