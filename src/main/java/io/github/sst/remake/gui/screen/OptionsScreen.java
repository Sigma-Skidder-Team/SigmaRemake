package io.github.sst.remake.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class OptionsScreen extends Screen {

    public OptionsScreen() {
        super(Text.of("Jello Options"));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

}
