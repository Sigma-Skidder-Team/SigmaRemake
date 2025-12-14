package io.github.sst.remake.event.impl.window;

import io.github.sst.remake.event.Cancellable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CharEvent extends Cancellable {
    public final long windowPointer;
    public final int codepoint, modifiers;
}
