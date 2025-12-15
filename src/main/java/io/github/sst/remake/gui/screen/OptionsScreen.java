package io.github.sst.remake.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class OptionsScreen extends Screen {
    public OptionsScreen() {
        super(new LiteralText("Jello Options"));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
