package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.render.Render2DEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new Render2DEvent(guiGraphics.pose(), deltaTracker.getGameTimeDeltaPartialTick(true)));
    }
}
