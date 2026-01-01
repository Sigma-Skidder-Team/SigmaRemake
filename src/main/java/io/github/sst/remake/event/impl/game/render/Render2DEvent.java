package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Render2DEvent extends Event {
    public final float tickDelta;
    public final long startTime;
    public final boolean tick;
}
