package io.github.sst.remake.mixin;

import io.github.sst.remake.tracker.impl.RotationTracker;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {
    @Inject(method = "drawEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;setRenderShadows(Z)V", ordinal = 0, shift = At.Shift.AFTER))
    private static void injectDrawEntityBefore(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        RotationTracker.renderingGui = true;
    }

    @Inject(method = "drawEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V", shift = At.Shift.AFTER))
    private static void injectDrawEntityAfter(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        RotationTracker.renderingGui = false;
    }
}
