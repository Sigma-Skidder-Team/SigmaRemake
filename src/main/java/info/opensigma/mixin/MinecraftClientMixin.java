package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(
            method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;instance:Lnet/minecraft/client/MinecraftClient;")
    )
    public final void injectOnMinecraftStartup(final CallbackInfo callbackInfo) {
        OpenSigma.getInstance().onMinecraftStartup();
    }

    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V", shift = At.Shift.BEFORE)
    )
    public final void injectOnMinecraftLoad(final CallbackInfo callbackInfo) {
        OpenSigma.getInstance().onMinecraftLoad();
    }

    @Inject(
            method = "isMultiplayerEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void injectMultiplayerBypass(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

}
