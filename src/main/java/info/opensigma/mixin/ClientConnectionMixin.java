package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.packet.ReceivePacketEvent;
import info.opensigma.event.impl.packet.SendPacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(
            method = "sendInternal",
            at = @At(value = "HEAD")
    )
    public void onSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new SendPacketEvent(packet));
    }
    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At(value = "HEAD")
    )
    public void onReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new ReceivePacketEvent(packet));
    }
}
