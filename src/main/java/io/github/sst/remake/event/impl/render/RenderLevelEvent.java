package io.github.sst.remake.event.impl.render;

import io.github.sst.remake.event.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RenderLevelEvent extends Event {
    public final float tickDelta;
    public final long startTime;
}
