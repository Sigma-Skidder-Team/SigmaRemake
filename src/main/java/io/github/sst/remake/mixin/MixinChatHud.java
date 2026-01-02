package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.ModifyChatYEvent;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class MixinChatHud {

    @ModifyVariable(
            method = "render",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private double modifyChatY(double s) {
        ModifyChatYEvent event = new ModifyChatYEvent(s);
        event.call();
        return event.getOffset();
    }

}
