package io.github.sst.remake.mixin;

import io.github.sst.remake.Client;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntityCommand {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void injectSendChatMessage(String message, CallbackInfo ci) {
        if (Client.INSTANCE.commandManager.execute(message)) {
            ci.cancel();
        }
    }
}