package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.util.game.player.MovementUtils;

public class MinemenSpeed extends SubModule {
    public MinemenSpeed() {
        super("Minemen");
    }

    @Override
    public void onEnable() {
        // no internal state to initialize, provided for parity
    }

    @Override
    public void onDisable() {
        // no state to restore
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null) return;
        if (client.player.isOnGround()) {
            double calculatedSpeed = 0.3399 + MovementUtils.getSpeedBoost() * 0.06;
            if (client.player.age % 3 == 0) {
                calculatedSpeed = 0.679 + MovementUtils.getSpeedBoost() * 0.12;
            }
            MovementUtils.setMotion(event, calculatedSpeed);
        }
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset any counters
        }
    }
}
