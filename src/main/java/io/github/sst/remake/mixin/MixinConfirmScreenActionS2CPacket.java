package io.github.sst.remake.mixin;

import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreenActionS2CPacket.class)
public class MixinConfirmScreenActionS2CPacket {
    @Shadow
    private int syncId;

    @Shadow
    private short actionId;

    @Shadow
    private boolean accepted;

    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    private void injectRead(PacketByteBuf buf, CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;
        ci.cancel();

        if (ViaInstance.getTargetVersion().newerThanOrEqualTo(ViaProtocols.R1_17)) {
            syncId = buf.readUnsignedByte();
        } else {
            syncId = buf.readUnsignedByte();
            actionId = buf.readShort();
            accepted = buf.readBoolean();
        }
    }
}