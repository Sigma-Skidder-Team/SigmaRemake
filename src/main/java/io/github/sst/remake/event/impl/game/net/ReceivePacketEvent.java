package io.github.sst.remake.event.impl.game.net;

import io.github.sst.remake.event.Cancellable;
import lombok.AllArgsConstructor;
import net.minecraft.network.Packet;

@AllArgsConstructor
public class ReceivePacketEvent extends Cancellable {
    public Packet<?> packet;
}
