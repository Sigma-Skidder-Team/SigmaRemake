package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.render.Render2DEvent;
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
        OpenSigma.getInstance().getEventBus().post(new Render2DEvent(matrices, tickDelta));
    }
}
