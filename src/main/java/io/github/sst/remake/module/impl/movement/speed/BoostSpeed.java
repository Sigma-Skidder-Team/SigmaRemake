package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class BoostSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("Auto Jump", "Automatically jumps for you.", true);
    public final BooleanSetting assumeSprinting = new BooleanSetting("Assume Sprinting", "Assume you are sprinting", false);
    public final BooleanSetting ignoreSneaking = new BooleanSetting("Ignore Sneaking", "Ignore sneaking", true);
    public final BooleanSetting ignoreInWater = new BooleanSetting("Ignore In Water", "Ignore being in water", true);
    public final SliderSetting boostAfterTicks = new SliderSetting("Boost After Ticks", "Boost after ticks since last boost", 15f, 1f, 40f, 1f);
    public final SliderSetting boostSpeed = new SliderSetting("Boost Speed", "Boost speed", 1.5f, 1f, 10f, 0.01f);
    public final SliderSetting normalSpeed = new SliderSetting("Normal Speed", "Normal speed", 1.2f, 1f, 10f, 0.01f);

    private int ticksSinceBoost = 0;
    private boolean initialized = false;

    public BoostSpeed() {
        super("Boost");
    }

    @Override
    public void onEnable() {
        ticksSinceBoost = 0;
        initialized = false;
    }

    @Override
    public void onDisable() {
        // restore any modified state
        ticksSinceBoost = 0;
        initialized = false;
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (!initialized) {
            ticksSinceBoost = 0;
            initialized = true;
        }
        double calculatedSpeed = MovementUtils.getSpeed();
        if (!client.player.isSprinting() && assumeSprinting.value) {
            calculatedSpeed += 0.15;
        }
        if (client.player.isSneaking() && ignoreSneaking.value) {
            calculatedSpeed *= 1.25;
        }
        if (client.player.isTouchingWater() && ignoreInWater.value) {
            calculatedSpeed *= 1.3;
        }
        if (client.player.isOnGround() && ticksSinceBoost >= boostAfterTicks.value) {
            calculatedSpeed *= boostSpeed.value;
            ticksSinceBoost = 0;
        } else {
            calculatedSpeed *= normalSpeed.value;
            ticksSinceBoost++;
        }
        MovementUtils.setMotion(event, calculatedSpeed);
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset boost state on lagback
            this.ticksSinceBoost = 0;
            this.initialized = false;
        }
    }
}
