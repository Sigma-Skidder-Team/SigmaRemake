package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.OldRender2DEvent;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin implements IMinecraft {
    @Shadow @Final private DebugHud debugHud;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        SigmaReborn.INSTANCE.screenHandler.debugHud = this.debugHud;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        new OldRender2DEvent(matrices).post();
    }
}