package info.opensigma.event.impl.packet

import info.opensigma.event.type.CancellableEvent
import net.minecraft.network.Packet

class ReceivePacketEvent(val packet: Packet<*>) : CancellableEvent()