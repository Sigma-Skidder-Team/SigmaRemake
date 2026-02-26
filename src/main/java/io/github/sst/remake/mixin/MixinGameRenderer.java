package io.github.sst.remake.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.event.impl.game.player.MovementFovEvent;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.render.RenderLevelEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void injectRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // because called right after getProfiler().push("level");
        new RenderLevelEvent(tickDelta, startTime).call();
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;", ordinal = 0, shift = At.Shift.BEFORE, opcode = Opcodes.GETFIELD))
    private void injectBeforeUIRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        new Render2DEvent(tickDelta, startTime, tick).call();
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", ordinal = 0, shift = At.Shift.BEFORE, opcode = Opcodes.GETFIELD))
    private void injectBeforeRenderHand(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrix.peek().getModel());
        if (client != null && client.world != null && client.player != null) {
            GL11.glTranslatef(0.0F, 0.0F, 0.0F);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            GL11.glDisable(2896);
            new Render3DEvent().call();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
        }
        RenderSystem.popMatrix();
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 1, opcode = Opcodes.GETFIELD))
    private Screen wrapRender(MinecraftClient instance, Operation<Screen> original) {
        if (Client.INSTANCE.screenManager.currentScreen == null) {
            return original.call(this.client);
        } else {
            return null;
        }
    }

    @ModifyExpressionValue(method = "updateMovementFovMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSpeed()F"))
    private float modifyGetSpeed(float original) {
        MovementFovEvent event = new MovementFovEvent(original);
        event.call();
        return event.speed;
    }
}