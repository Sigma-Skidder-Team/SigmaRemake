package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.managers.FontManager;
import com.skidders.sigma.managers.ModuleManager;
import com.skidders.sigma.processors.ScreenProcessor;
import com.skidders.sigma.utils.file.FileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(
            method = "isMultiplayerEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void injectMultiplayerBypass(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
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
        SigmaReborn.INSTANCE.fontManager = new FontManager();

        SigmaReborn.INSTANCE.moduleManager = new ModuleManager();
        SigmaReborn.INSTANCE.screenProcessor = new ScreenProcessor();
        SigmaReborn.EVENT_BUS.register(SigmaReborn.INSTANCE.moduleManager);
        SigmaReborn.EVENT_BUS.register(SigmaReborn.INSTANCE.screenProcessor);
    }

}
