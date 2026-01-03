package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Cancellable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MouseHoverEvent extends Cancellable {
    public final int button;
}
