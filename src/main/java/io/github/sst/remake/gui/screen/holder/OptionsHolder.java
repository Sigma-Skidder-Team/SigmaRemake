package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class OptionsHolder extends Screen {
    public OptionsHolder() {
        super(new LiteralText("Jello Options"));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
