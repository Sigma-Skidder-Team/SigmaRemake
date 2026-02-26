package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.player.JumpEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    @Unique
    private JumpEvent jumpEvent;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void injectJump(CallbackInfo ci) {
        jumpEvent = new JumpEvent((LivingEntity) (Object) this, yaw);
        jumpEvent.call();

        if (jumpEvent.cancelled) {
            ci.cancel();
        }
    }

    @Redirect(method = "jump", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;yaw:F"))
    private float redirectJump(LivingEntity self) {
        return jumpEvent.yaw;
    }
}