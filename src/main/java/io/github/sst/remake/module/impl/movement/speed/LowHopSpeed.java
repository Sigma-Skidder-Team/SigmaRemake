package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;

import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import net.minecraft.util.math.BlockPos;

public class LowHopSpeed extends SubModule {
    public final BooleanSetting lowTargetStrafe = new BooleanSetting("Low TargetStrafe", "Keeps lowhopping when using space to targetstrafe", true);

    private int tickCounter = 1;
    private double moveSpeed = 0.0;
    private boolean initialized = false;

    public LowHopSpeed() {
        super("LowHop");
    }

    @Override
    public void onEnable() {
        this.tickCounter = 1;
        this.initialized = false;
        this.moveSpeed = 0.0;
    }

    @Override
    public void onDisable() {
        // restore defaults
        this.initialized = false;
        this.moveSpeed = MovementUtils.getSpeed();
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null || !MovementUtils.isMoving() || client.player.isTouchingWater()) return;
        if (!initialized) {
            double mx = client.player.getVelocity().x;
            double mz = client.player.getVelocity().z;
            this.moveSpeed = Math.sqrt(mx * mx + mz * mz);
            initialized = true;
        }
        if (client.player.isOnGround()) {
            tickCounter = 0;
            moveSpeed *= 1.05;
            boolean blockFlyEnabled = false;
            if (io.github.sst.remake.Client.INSTANCE.moduleManager != null) {
                io.github.sst.remake.module.Module m = io.github.sst.remake.Client.INSTANCE.moduleManager.getModule(io.github.sst.remake.module.impl.movement.BlockFlyModule.class);
                blockFlyEnabled = m != null && m.enabled;
            }
            if (isNearEdge() && !blockFlyEnabled) {
                event.setY(0.42);
            } else {
                event.setY(0.2);
            }
        } else {
            moveSpeed = Math.max(MovementUtils.getSpeed(), moveSpeed * 0.98);
        }
        MovementUtils.setMotion(event, moveSpeed);
    }

    @Subscribe
    public void onStep(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // legacy Step handling not present; JumpEvent used as approximation
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // Reset internal low-hop state on lagback/teleport
            this.initialized = false;
            this.tickCounter = 1;
            this.moveSpeed = MovementUtils.getSpeed();
        }
    }

    private boolean isNearEdge() {
        BlockPos pos = new BlockPos(client.player.getX(), client.player.getY() - 0.1, client.player.getZ());
        return client.world.isAir(pos.east()) || client.world.isAir(pos.west()) ||
                client.world.isAir(pos.north()) || client.world.isAir(pos.south());
    }

    
}
