package io.github.sst.remake.mixin;

import io.github.sst.remake.util.client.TimerSpeedAccess;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter implements TimerSpeedAccess {
    @Shadow
    public float lastFrameDuration;
    @Unique
    private float timerSpeed = 1.0f;

    @Override
    public float getTimerSpeed() {
        return timerSpeed;
    }

    @Override
    public void setTimerSpeed(float speed) {
        timerSpeed = speed;
    }

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F", opcode = org.objectweb.asm.Opcodes.PUTFIELD))
    private void injectBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.lastFrameDuration *= this.timerSpeed;
    }
}