package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Event;

public class RenderClient2DEvent extends Event {
    public int offset = 99;

    public void increment(int offset) {
        this.offset += offset;
    }
}