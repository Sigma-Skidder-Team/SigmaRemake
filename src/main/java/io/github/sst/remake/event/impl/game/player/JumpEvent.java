package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Cancellable;
import io.github.sst.remake.event.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class JumpEvent extends Cancellable {
    public final LivingEntity entity;
    public Vec3d velocity;
    public float yaw;

    public JumpEvent(LivingEntity entity, Vec3d velocity, float yaw) {
        this.entity = entity;
        this.velocity = velocity;
        this.yaw = yaw;
    }
}
