package io.github.sst.remake.mixin;

import io.github.sst.remake.util.client.TimerSpeedAccess;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Redirect(
            method = "beginRenderTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void redirectLastFrameDuration(RenderTickCounter instance, float value) {
        instance.lastFrameDuration = value * timerSpeed;
    }
}