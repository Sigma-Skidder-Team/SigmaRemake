package io.github.sst.remake.mixin;

import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ConfirmScreenActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreenActionC2SPacket.class)
public class MixinConfirmScreenActionC2SPacket {
    @Shadow
    private int syncId;

    @Shadow
    private short actionId;

    @Shadow
    private boolean accepted;

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void injectWrite(PacketByteBuf buf, CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;
        ci.cancel();

        if (ViaInstance.getTargetVersion().newerThanOrEqualTo(ViaProtocols.R1_17)) {
            buf.writeByte(syncId);
        } else {
            buf.writeByte(syncId);
            buf.writeShort(actionId);
            buf.writeByte(accepted ? 1 : 0);
        }
    }
}