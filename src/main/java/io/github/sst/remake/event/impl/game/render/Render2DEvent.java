package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Event;
import lombok.RequiredArgsConstructor;

/**
 * @apiNote DON'T USE THIS FOR REGULAR UI RENDERING, INSTEAD USE
 * @see io.github.sst.remake.event.impl.client.RenderClient2DEvent
 */
@RequiredArgsConstructor
public class Render2DEvent extends Event {
    public final float tickDelta;
    public final long startTime;
    public final boolean tick;
}
