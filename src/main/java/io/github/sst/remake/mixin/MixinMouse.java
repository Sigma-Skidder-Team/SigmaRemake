package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.window.MouseButtonEvent;
import io.github.sst.remake.event.impl.window.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void injectMouseScroll(long window, int button, int action, int mods, CallbackInfo ci) {
        MouseButtonEvent event = new MouseButtonEvent(window, button, action, mods);
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void injectMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(window, horizontal, vertical);
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }
}