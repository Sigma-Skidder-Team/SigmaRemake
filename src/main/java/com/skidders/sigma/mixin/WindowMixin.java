package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(
        method = "onWindowSizeChanged",
            at = @At("TAIL")
    )
    public final void onWindowSizeUpdate(long window, int width, int height, CallbackInfo ci) {
        SigmaReborn.INSTANCE.screenProcessor.onResize();
    }

}
