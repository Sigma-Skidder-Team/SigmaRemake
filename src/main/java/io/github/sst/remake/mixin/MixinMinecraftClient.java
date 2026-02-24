package io.github.sst.remake.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.State;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.RunLoopEvent;
import io.github.sst.remake.event.impl.OpenScreenEvent;
import io.github.sst.remake.event.impl.window.WindowResizeEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.gui.screen.loading.LoadingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Unique
    private RunLoopEvent runLoopEvent;

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
        runLoopEvent = new RunLoopEvent();
        runLoopEvent.call();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.AFTER))
    private void injectAfterRun(CallbackInfo ci) {
        runLoopEvent.state = State.POST;
        runLoopEvent.call();
    }

    @Inject(method = "onResolutionChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;resize(Lnet/minecraft/client/MinecraftClient;II)V", shift = At.Shift.AFTER))
    private void injectResolutionChange(CallbackInfo ci) {
        new WindowResizeEvent().call();
    }

    @Inject(method = "openScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void injectOpenScreen(CallbackInfo ci) {
        new OpenScreenEvent().call();
    }

    @Inject(method = "joinWorld", at = @At(value = "HEAD"))
    private void injectJoinWorld(CallbackInfo ci) {
        new LoadWorldEvent().call();
    }

    @Redirect(method = "setOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;", opcode = Opcodes.PUTFIELD))
    private void redirectOverlay(MinecraftClient instance, Overlay value) {
        if (value instanceof SplashScreen) {
            SplashScreen splash = (SplashScreen) value;
            value = new LoadingScreen(
                    splash.reload,
                    splash.exceptionHandler,
                    splash.reloading
            );
        }

        instance.overlay = value;
    }

    @ModifyArg(method = "getWindowTitle", at = @At(value = "INVOKE_STRING", target = "Ljava/lang/StringBuilder;<init>(Ljava/lang/String;)V", args = "ldc=Minecraft"))
    private String modifyWindowTitle(String title) {
        title = "Jello for Fabric " + Client.VERSION;
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

    @ModifyReturnValue(method = "getFramerateLimit", at = @At("RETURN"))
    private int modifyFramerateLimit(int original) {
        return (original == 60) ? 120 : original;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", shift = At.Shift.BEFORE))
    private void injectHandleInputs(CallbackInfo ci) {
        new ActionEvent().call();
    }
}