package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.render.Render2DEvent;
import info.opensigma.event.impl.render.Render3DEvent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks [{@link GameRenderer#renderWorld(RenderTickCounter)}] to post [{@link Render2DEvent}] events.
 * We aren't hooking [{@link GameRenderer#render(float, long, boolean)}
 * because something else could call this, & we wouldn't send the event when we should've,
 * even though it doesn't really matter.
 */

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    public void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new Render3DEvent(tickCounter.getTickDelta(true), tickCounter.getLastDuration()));
    }
}
