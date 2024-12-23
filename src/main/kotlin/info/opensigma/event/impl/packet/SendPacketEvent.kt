package info.opensigma.event.impl.packet

import info.opensigma.event.type.CancellableEvent
import net.minecraft.network.protocol.Packet

class SendPacketEvent : CancellableEvent {
    var packet: Packet<*>
    val packets: MutableList<Packet<*>> = mutableListOf()
    constructor(packet: Packet<*>) {
        this.packet = packet
        this.packets.add(packet)
    }
}
