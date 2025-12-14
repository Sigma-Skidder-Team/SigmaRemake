package io.github.sst.remake.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sst.remake.Client;
import io.github.sst.remake.event.impl.RunLoopEvent;
import io.github.sst.remake.event.impl.window.WindowResizeEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;registerReloader(Lnet/minecraft/resource/ResourceReloader;)V", ordinal = 16))
    private void injectStart(CallbackInfo ci) {
        Client.INSTANCE.start();
    }

    @Shadow
    private volatile boolean running;

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void injectShutdown(CallbackInfo ci) {
        if (this.running) {
            Client.INSTANCE.shutdown();
        }
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private void injectRun(CallbackInfo ci) {
        new RunLoopEvent(true).call();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.AFTER))
    private void injectAfterRun(CallbackInfo ci) {
        new RunLoopEvent(false).call();
    }

    @Inject(method = "onResolutionChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;resize(Lnet/minecraft/client/MinecraftClient;II)V", shift = At.Shift.AFTER))
    private void injectResolutionChange(CallbackInfo ci) {
        new WindowResizeEvent().call();
    }

    @ModifyArg(method = "getWindowTitle", at = @At(value = "INVOKE_STRING", target = "Ljava/lang/StringBuilder;<init>(Ljava/lang/String;)V", args = "ldc=Minecraft"))
    private String modifyWindowTitle(String title) {
        title = "Jello for Sigma " + Client.VERSION;
        return title;
    }

    @ModifyReturnValue(method = "isModded", at = @At("RETURN"))
    private boolean modifyIsModded(boolean original) {
        return false;
    }

    @ModifyReturnValue(method = "hasReducedDebugInfo", at = @At("RETURN"))
    private boolean modifyHasReducedDebugInfo(boolean original) {
        return false;
    }

    @ModifyReturnValue(method = "isDemo", at = @At("RETURN"))
    private boolean modifyIsDemo(boolean original) {
        return false;
    }

    @ModifyReturnValue(method = "isMultiplayerEnabled", at = @At("RETURN"))
    private boolean modifyIsMultiplayerEnabled(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "isOnlineChatEnabled", at = @At("RETURN"))
    private boolean modifyIsOnlineChatEnabled(boolean original) {
        return true;
    }

}
