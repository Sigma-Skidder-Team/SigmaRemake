package com.skidders.sigma.mixin;

import com.skidders.sigma.event.impl.Render3DEvent;
import com.skidders.sigma.util.client.interfaces.IAccessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements IAccessor {
    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    public void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        new Render3DEvent(matrix, tickDelta, limitTime).post();
    }

    @Shadow
    private void loadShader(Identifier identifier) {

    }

    @Override
    public void sigmaRemake$invokeLoadShader(Identifier identifier) {
        loadShader(identifier);
    }
}
