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
import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.fixes.AttackOrderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "stop", at = @At("HEAD"))
    private void injectStop(CallbackInfo ci) {
        Client.INSTANCE.shutdown();
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
        title = "Jello for Sigma Fabric " + Client.VERSION;
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

    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void injectDoAttackAttackEntity(CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;
        ci.cancel();

        AttackOrderUtils.sendFixedAttack(this.player, ((EntityHitResult) this.crosshairTarget).getEntity(), Hand.MAIN_HAND);
    }

    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void injectDoAttackSwing(CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;
        ci.cancel();

        AttackOrderUtils.sendConditionalSwing(this.crosshairTarget, Hand.MAIN_HAND);
    }
}