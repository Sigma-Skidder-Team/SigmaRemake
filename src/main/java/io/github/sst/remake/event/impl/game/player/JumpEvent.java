package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Event;
import net.minecraft.entity.LivingEntity;

public class JumpEvent extends Event {
    public final LivingEntity entity;
    public float yaw;

    public JumpEvent(LivingEntity entity, float yaw) {
        this.entity = entity;
        this.yaw = yaw;
    }
}
