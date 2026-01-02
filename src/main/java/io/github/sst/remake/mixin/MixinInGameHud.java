package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.render.RenderScoreboardEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void injectRenderScoreboard(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderScoreboardEvent event = new RenderScoreboardEvent(false);
        event.call();

        if (event.cancelled)
            ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", shift = At.Shift.AFTER))
    private void injectAfterRenderScoreboard(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderScoreboardEvent event = new RenderScoreboardEvent(true);
        event.call();
    }

}
