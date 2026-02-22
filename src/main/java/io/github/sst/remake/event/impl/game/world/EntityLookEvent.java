package io.github.sst.remake.event.impl.game.world;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;

@AllArgsConstructor
public class EntityLookEvent extends Cancellable {
    public final Entity entity;
    public final float tickDelta;
    public float yaw, pitch;
}
