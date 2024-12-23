package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.packet.ReceivePacketEvent;
import info.opensigma.event.impl.packet.SendPacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {
    @Inject(
            method = "doSendPacket",
            at = @At(value = "HEAD")
    )
    public void onSend(Packet<?> p_packet, PacketSendListener sendListener, boolean flush, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new SendPacketEvent(p_packet));
    }
    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "HEAD")
    )
    public void onReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        OpenSigma.getInstance().getEventBus().post(new ReceivePacketEvent(packet));
    }
}
