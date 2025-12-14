package io.github.sst.remake.event.impl.window;

import io.github.sst.remake.event.Cancellable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MouseScrollEvent extends Cancellable {
    public final long window;
    public final double horizontal, vertical;
}
