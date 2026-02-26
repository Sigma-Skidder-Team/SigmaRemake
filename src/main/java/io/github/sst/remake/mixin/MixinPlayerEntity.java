package io.github.sst.remake.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.sst.remake.event.impl.game.player.SafeWalkEvent;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @ModifyExpressionValue(method = "adjustMovementForSneaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_30263()Z"))
    private boolean gateSneakEdgeAdjustment(boolean original) {
        SafeWalkEvent safeWalkEvent = new SafeWalkEvent(true);
        safeWalkEvent.call();

        return safeWalkEvent.situation == SafeWalkEvent.Situation.PLAYER || original;
    }

    @Inject(method = "adjustMovementForSneaking", at = @At("TAIL"))
    private void injectAdjustMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir) {
        new SafeWalkEvent(false).call();
    }
}