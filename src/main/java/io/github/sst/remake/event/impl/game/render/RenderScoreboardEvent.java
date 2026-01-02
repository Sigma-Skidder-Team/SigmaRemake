package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RenderScoreboardEvent extends Cancellable {
    public final boolean post;
}
