package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.player.JumpEvent;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Redirect(method = "jump", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;yaw:F"))
    private float redirectJump(LivingEntity self) {
        JumpEvent event = new JumpEvent(self, self.yaw);
        event.call();

        return event.yaw;
    }
}