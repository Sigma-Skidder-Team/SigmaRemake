package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;
import net.minecraft.entity.LivingEntity;

public class RenderEntityPitchEvent extends Cancellable {
    public LivingEntity livingEntity;
    public float tickDelta;
    public float result;

    public RenderEntityPitchEvent(LivingEntity livingEntity, float tickDelta) {
        this.livingEntity = livingEntity;
        this.tickDelta = tickDelta;
    }
}
