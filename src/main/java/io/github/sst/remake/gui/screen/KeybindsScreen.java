package io.github.sst.remake.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class KeybindsScreen extends Screen {
    public KeybindsScreen() {
        super(Text.of("Jello Keyboard"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
