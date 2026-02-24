package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.player.VelocityYawEvent;
import io.github.sst.remake.event.impl.game.world.EntityLookEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    protected abstract Vec3d getRotationVector(float pitch, float yaw);

    @Shadow
    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Inject(method = "getRotationVec", at = @At("HEAD"), cancellable = true)
    private void injectGetRotationVec(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        Entity entity = (Entity) (Object) this;
        EntityLookEvent event = new EntityLookEvent(entity, tickDelta, entity.yaw, entity.pitch);
        event.call();

        if (event.cancelled) {
            cir.setReturnValue(getRotationVector(event.pitch, event.yaw));
        }
    }

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d hookEventVelocityYaw(Vec3d movementInput, float speed, float yaw) {
        VelocityYawEvent event = new VelocityYawEvent((Entity) (Object) this, movementInput, yaw, speed);
        event.call();

        return movementInputToVelocity(event.movementInput, event.speed, event.yaw);
    }
}