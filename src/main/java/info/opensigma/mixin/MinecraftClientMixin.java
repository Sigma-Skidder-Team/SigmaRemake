package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(
            method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;instance:Lnet/minecraft/client/Minecraft;")
    )
    public final void injectOnMinecraftStartup(final CallbackInfo callbackInfo) {
        OpenSigma.getInstance().onMinecraftStartup();
    }

    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V", shift = At.Shift.BEFORE)
    )
    public final void injectOnMinecraftLoad(final CallbackInfo callbackInfo) {
        OpenSigma.getInstance().onMinecraftLoad();
    }

    @Inject(
            method = "allowsMultiplayer",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void injectMultiplayerBypass(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(
            method = "<init>",
            at = @At(value = "TAIL")
    )
    public final void onMinecraftClientInitEnd(GameConfig gameConfig, CallbackInfo ci) {
        //OpenSigma.getInstance().getFontManager().init();
    }

}
