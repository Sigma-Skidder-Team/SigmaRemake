package io.github.sst.remake.gui.screen.holder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGuiHolder extends Screen {
    public ClickGuiHolder(Text title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
