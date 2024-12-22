package info.opensigma.event.impl.packet

import info.opensigma.event.type.CancellableEvent
import net.minecraft.network.Packet

class SendPacketEvent(var packet: Packet<*>, val packets: List<Packet<*>>) : CancellableEvent()
