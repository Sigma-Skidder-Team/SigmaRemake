package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.render.Render2DEvent;
import io.github.sst.remake.event.impl.render.RenderLevelEvent;
import net.minecraft.client.render.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void injectRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // because called right after getProfiler().push("level");
        new RenderLevelEvent(tickDelta, startTime).call();
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;", ordinal = 0, shift = At.Shift.BEFORE, opcode = Opcodes.GETFIELD))
    private void injectBeforeUIRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        new Render2DEvent(tickDelta, startTime, tick).call();
    }


}
