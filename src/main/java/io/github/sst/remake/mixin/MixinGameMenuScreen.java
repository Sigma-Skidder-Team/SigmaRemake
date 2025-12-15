package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.client.InitPauseMenuWidgetsEvent;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen{

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void injectWidgets(CallbackInfo ci) {
        new InitPauseMenuWidgetsEvent((GameMenuScreen) (Object) this).call();
    }

}
