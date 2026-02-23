package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.client.InputEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(boolean slowDown, CallbackInfo ci) {
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
