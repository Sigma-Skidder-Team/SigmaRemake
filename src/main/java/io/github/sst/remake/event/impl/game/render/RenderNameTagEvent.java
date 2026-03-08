package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;

@AllArgsConstructor
public class RenderNameTagEvent extends Cancellable {
    public final Entity entity;
}
