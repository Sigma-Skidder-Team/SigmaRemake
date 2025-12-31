package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;

public class OptionsHolder extends Screen {
    public OptionsHolder() {
        super(Text.literal("Jello Options"));
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
