package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Inject(method = "getRightText", at = @At("TAIL"))
    private void getRightText(CallbackInfoReturnable<List<String>> ci) {
        SigmaReborn.INSTANCE.screenHandler.rightText = ci.getReturnValue();
    }

}
