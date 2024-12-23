package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.render.Render2DEvent;
import info.opensigma.event.impl.render.Render3DEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks [{@link GameRenderer#renderLevel(DeltaTracker)}}] to post [{@link Render2DEvent}] events.
 */

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    public void onRenderWorld(DeltaTracker deltaTracker, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new Render3DEvent(deltaTracker.getGameTimeDeltaPartialTick(true), deltaTracker.getGameTimeDeltaTicks()));
    }
}
