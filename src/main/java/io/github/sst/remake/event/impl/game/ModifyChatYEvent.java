package io.github.sst.remake.event.impl.game;

import io.github.sst.remake.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModifyChatYEvent extends Event {
    private double offset;

    public void increment(double offset) {
        this.offset += offset;
    }
}
