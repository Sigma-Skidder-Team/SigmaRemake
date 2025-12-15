package io.github.sst.remake.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class KeybindsScreen extends Screen {
    public KeybindsScreen() {
        super(new LiteralText("Jello Keyboard"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
