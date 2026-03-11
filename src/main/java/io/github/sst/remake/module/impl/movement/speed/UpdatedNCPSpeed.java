package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class UpdatedNCPSpeed extends SubModule {
    public final ModeSetting updatedNcpMode = new ModeSetting("Mode", "Speed mode", 0, "Basic", "Low", "Ground");

    public UpdatedNCPSpeed() {
        super("UpdatedNCP");
    }

    @Override
    public void onDisable() {
        // restore default timer when this submodule is disabled
        setTimer(1.0f);
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        if (client.player == null) return;
        switch (updatedNcpMode.value) {
            case "Basic":
                if (client.player.isOnGround()) {
                    if (MovementUtils.isMoving()) {
                        client.player.jump();
                        event.y = MovementUtils.getJumpValue();
                        MovementUtils.strafe(0.48);
                    }
                    setTimer(1.09f);
                }
                break;
            case "Low":
                if (client.player.isOnGround()) {
                    if (MovementUtils.isMoving()) {
                        event.y = 0.4;
                        MovementUtils.strafe(0.48);
                    }
                    setTimer(1.09f);
                }
                break;
            case "Ground":
                if (client.player.isOnGround()) {
                    if (MovementUtils.isMoving()) {
                        event.y = 0.05;
                        MovementUtils.strafe(0.25);
                    }
                    setTimer(1.09f);
                }
                break;
        }
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // ensure jump value adjustments when needed
        // no-op unless specific behavior required
    }
}
