package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.window.WindowResizeEvent;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {

    @Inject(method = "onWindowSizeChanged", at = @At("RETURN"))
    private void injectWindowSizeChange(CallbackInfo ci) {
        new WindowResizeEvent().call();
    }

}
