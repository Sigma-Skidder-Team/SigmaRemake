package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.Render2DEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        matrices.push();
        SigmaReborn.INSTANCE.screenProcessor.renderWatermark();
        matrices.pop();

        SigmaReborn.EVENT_BUS.post(new Render2DEvent(matrices));
    }

}
