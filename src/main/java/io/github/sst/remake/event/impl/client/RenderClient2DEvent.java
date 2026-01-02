package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenderClient2DEvent extends Event {
    private int offset = 99;

    public void increment(int offset) {
        this.offset += offset;
    }
}
