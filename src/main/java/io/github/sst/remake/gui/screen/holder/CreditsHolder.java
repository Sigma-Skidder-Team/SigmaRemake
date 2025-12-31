package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CreditsHolder extends Screen {
    public CreditsHolder(Text title) {
        super(title);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
