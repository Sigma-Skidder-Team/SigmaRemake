package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.KeyPressEvent;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            final KeyPressEvent keyPressEvent = new KeyPressEvent(key, modifiers, action);
            SigmaReborn.EVENT_BUS.post(keyPressEvent);
        }
    }

}
