package io.github.sst.remake.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.event.impl.game.render.RenderHudEvent;
import io.github.sst.remake.event.impl.game.render.RenderScoreboardEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void injectRenderScoreboard(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderScoreboardEvent event = new RenderScoreboardEvent(false);
        event.call();

        if (event.cancelled)
            ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getArmorStack(I)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE))
    private void injectAfterRenderVignette(CallbackInfo ci) {
        GL11.glPushMatrix();

        double scale = client.getWindow().getScaleFactor() / (double) ((float) Math.pow(client.getWindow().getScaleFactor(), 2.0));
        GL11.glScaled(scale, scale, scale);
        GL11.glScaled(Client.INSTANCE.screenManager.scaleFactor, Client.INSTANCE.screenManager.scaleFactor, Client.INSTANCE.screenManager.scaleFactor);
        GL11.glDisable(2912);

        RenderSystem.disableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
        RenderSystem.alphaFunc(519, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(2896);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

        new RenderHudEvent().call();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.alphaFunc(518, 0.1F);

        GL11.glPopMatrix();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", shift = At.Shift.AFTER))
    private void injectAfterRenderScoreboard(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderScoreboardEvent event = new RenderScoreboardEvent(true);
        event.call();
    }
}