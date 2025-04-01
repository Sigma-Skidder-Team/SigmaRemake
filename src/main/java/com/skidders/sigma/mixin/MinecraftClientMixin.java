package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.RunEvent;
import com.skidders.sigma.screen.pages.LoadingPage;
import com.skidders.sigma.util.system.file.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Accessor("resourceManager")
    public abstract ReloadableResourceManager getResourceManager();

    @Accessor("resourceReloadFuture")
    public abstract CompletableFuture<Void> getResourceReloadFuture();

    @Accessor("resourceReloadFuture")
    public abstract void setResourceReloadFuture(CompletableFuture<Void> ignored);

    @Accessor("COMPLETED_UNIT_FUTURE")
    public abstract CompletableFuture<Unit> getCompletedUnitFuture();

    @Shadow
    private void handleResourceReloadException(Throwable throwable) {}

    @Shadow
    private void checkGameData() {}

    @Shadow public abstract ResourcePackManager getResourcePackManager();

    @Inject(
            method = "<init>",
            at = @At(value = "TAIL")
    )
    public void onMinecraftClientInitEnd(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        List<ResourcePack> list = getResourcePackManager().createResourcePacks();
        client.setOverlay(new LoadingPage(
                client,
                getResourceManager().beginMonitoredReload(Util.getMainWorkerExecutor(), client, getCompletedUnitFuture(), list),
                optional -> Util.ifPresentOrElse(optional, this::handleResourceReloadException, () -> {
                    if (SharedConstants.isDevelopment) {
                        this.checkGameData();
                    }
                }),
                false
        ));
    }

    @Inject(
            method = "reloadResources",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onReloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (getResourceReloadFuture() != null) {
            cir.setReturnValue(getResourceReloadFuture());
        } else {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            if (client.overlay instanceof LoadingPage) {
                setResourceReloadFuture(completableFuture);
                cir.setReturnValue(completableFuture);
            } else {
                getResourcePackManager().scanPacks();  // Use accessor for resourcePackManager
                List<ResourcePack> list = getResourcePackManager().createResourcePacks();  // Create the resource packs list
                client.setOverlay(new LoadingPage(  // Replace SplashScreen with LoadingPage
                        client,
                        getResourceManager().beginMonitoredReload(Util.getMainWorkerExecutor(), client, getCompletedUnitFuture(), list),
                        optional -> Util.ifPresentOrElse(optional, this::handleResourceReloadException, () -> {
                            client.worldRenderer.reload();
                            completableFuture.complete(null);
                        }),
                        true
                ));
                setResourceReloadFuture(completableFuture);
                cir.setReturnValue(completableFuture);
            }
        }
    }

    @Inject(
            method = "isMultiplayerEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void isMultiplayerEnabled(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(
            method = "isDemo",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void isDemo(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(
            method = "<init>",
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;gameVersion:Ljava/lang/String;")
    )
    public final void onMinecraftClientInit(RunArgs args, CallbackInfo ci) {
        FileUtil.createFolder("sigma");
        FileUtil.createFolder("sigma/fonts");
    }

    @Inject(
            method = "<init>",
            at = @At(value = "TAIL")
    )
    public final void onMinecraftClientInitEnd(RunArgs args, CallbackInfo ci) {
        SigmaReborn.INSTANCE.onLastInitialize();
    }

    @Inject(method = "isModded", at = @At(value = "HEAD"), cancellable = true)
    public void isModded(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;startMonitor(ZLnet/minecraft/util/TickDurationMonitor;)V"))
    public void run(CallbackInfo cir) {
        new RunEvent().post();
    }
}
