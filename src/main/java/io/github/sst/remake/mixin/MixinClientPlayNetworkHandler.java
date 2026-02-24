package io.github.sst.remake.mixin;

import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ConfirmScreenActionC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onConfirmScreenAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
    private void injectonConfirmScreenAction(ConfirmScreenActionS2CPacket packet, CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;

        if (ViaInstance.getTargetVersion().newerThanOrEqualTo(ViaProtocols.R1_17)) {
            this.sendPacket(new ConfirmScreenActionC2SPacket(packet.getSyncId(), (short) 0, false));
            ci.cancel();
        }
    }
}