package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class InvadedSpeed extends SubModule {
    public final SliderSetting speed = new SliderSetting("Speed", "Speed value", 3.0f, 0.5f, 9.5f, 0.1f);
    private int tick = 0;

    public InvadedSpeed() {
        super("Invaded");
    }

    @Override
    public void onDisable() {
        MovementUtils.strafe(0.28);
        // Reset timer logic if needed
    }

    @Override
    public void onEnable() {
        this.tick = 0;
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset on lagback/teleport
            this.tick = 0;
        }
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        tick++;
        if (tick != 1) {
            if (tick != 2) {
                if (tick >= 3) {
                    tick = 0;
                    MovementUtils.setMotion(event, speed.value);
                }
            } else {
                MovementUtils.setMotion(event, MovementUtils.getSpeed() + 0.05);
            }
        } else {
            MovementUtils.setMotion(event, MovementUtils.getSpeed() + 0.05);
        }
    }

    // single ReceivePacketEvent handler above handles lagback resets
}
