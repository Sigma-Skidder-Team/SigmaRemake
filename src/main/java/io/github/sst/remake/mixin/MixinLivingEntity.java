package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.tracker.impl.RotationTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "turnHead(FF)F",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/LivingEntity;yaw:F",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private float redirectTurnHeadYawRead0(LivingEntity instance) {
        return getTurnHeadYaw(instance);
    }

    @Redirect(
            method = "turnHead(FF)F",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/LivingEntity;yaw:F",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 1
            )
    )
    private float redirectTurnHeadYawRead1(LivingEntity instance) {
        return getTurnHeadYaw(instance);
    }

    private float getTurnHeadYaw(Entity instance) {
        if (instance == MinecraftClient.getInstance().player) {
            return RotationTracker.yaw;
        }
        return instance.yaw;
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void hookJump(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        float f = self.getJumpVelocity();
        if (self.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            f += 0.1f * (float)(self.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1);
        }

        Vec3d current = self.getVelocity();
        Vec3d jumpVec = new Vec3d(current.x, (double) f, current.z);

        JumpEvent event = new JumpEvent(self, jumpVec, self.yaw);
        event.call();

        if (event.cancelled) {
            ci.cancel();
            return;
        }

        self.setVelocity(event.velocity);

        if (self.isSprinting()) {
            float g = event.yaw * ((float)Math.PI / 180.0f);
            self.setVelocity(self.getVelocity().add(
                    (double)(-MathHelper.sin(g) * 0.2f),
                    0.0,
                    (double)( MathHelper.cos(g) * 0.2f)
            ));
        }

        self.velocityDirty = true;

        ci.cancel();
    }
}