package io.github.sst.remake.event.impl;

import io.github.sst.remake.event.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class PreOpenScreenEvent extends Cancellable {
    public final Screen screen;

    public PreOpenScreenEvent(Screen screen) {
        this.screen = screen;
    }
}
