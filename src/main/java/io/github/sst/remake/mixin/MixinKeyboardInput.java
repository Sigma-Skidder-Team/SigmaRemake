package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.client.InputEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
    @Shadow
    @Final
    private GameOptions settings;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickTail(boolean slowDown, CallbackInfo ci) {
        ci.cancel();

        this.movementForward = 0.0f;
        this.movementSideways = 0.0f;

        this.pressingForward = this.settings.keyForward.isPressed();
        this.pressingBack = this.settings.keyBack.isPressed();
        this.pressingLeft = this.settings.keyLeft.isPressed();
        this.pressingRight = this.settings.keyRight.isPressed();

        if (this.pressingForward) {
            ++this.movementForward;
        }

        if (this.pressingBack) {
            --this.movementForward;
        }

        if (this.pressingLeft) {
            ++this.movementSideways;
        }

        if (this.pressingRight) {
            --this.movementSideways;
        }

        this.jumping = this.settings.keyJump.isPressed();
        this.sneaking = this.settings.keySneak.isPressed();

        InputEvent event = new InputEvent(this.movementForward, this.movementSideways, this.jumping, this.sneaking, 0.3F);
        event.call();

        this.movementSideways = event.strafe;
        this.movementForward = event.forward;
        this.jumping = event.jumping;
        this.sneaking = event.sneaking;

        if (this.sneaking) {
            this.movementSideways *= event.sneakFactor;
            this.movementForward *= event.sneakFactor;
        }
    }
}