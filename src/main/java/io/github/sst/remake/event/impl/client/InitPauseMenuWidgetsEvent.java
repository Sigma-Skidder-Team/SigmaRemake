package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.GameMenuScreen;

@RequiredArgsConstructor
public class InitPauseMenuWidgetsEvent extends Event {
    public final GameMenuScreen screen;
}
