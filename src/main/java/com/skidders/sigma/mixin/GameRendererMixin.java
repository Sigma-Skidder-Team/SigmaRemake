package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.Render3DEvent;
import com.skidders.sigma.screens.GameRendererAccessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    public void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        SigmaReborn.EVENT_BUS.post(new Render3DEvent(matrix, tickDelta, limitTime));
    }

    @Shadow
    private void loadShader(Identifier identifier) {

    }

    @Override
    public void sigmaRemake$invokeLoadShader(Identifier identifier) {
        loadShader(identifier);
    }

}
