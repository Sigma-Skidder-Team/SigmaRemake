package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Cancellable;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.LivingEntity;

@RequiredArgsConstructor
public class HasLabelEvent extends Cancellable {
    public final LivingEntity entity;
}