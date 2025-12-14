package io.github.sst.remake.event.impl.window;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KeyEvent extends Cancellable {
    public final long window;
    public final int key, scancode, action, modifiers;
}
