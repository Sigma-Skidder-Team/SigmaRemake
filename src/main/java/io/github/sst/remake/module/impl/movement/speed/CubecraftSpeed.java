package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.util.game.world.WorldUtils;

public class CubecraftSpeed extends SubModule {
    public final ModeSetting cubecraftMode = new ModeSetting("Mode", "Speed mode", 0, "Basic", "Hop", "YPort");
    public final SliderSetting speed = new SliderSetting("Speed", "Speed value", 0.75f, 0.1f, 1.0f, 0.01f);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you", false);

    private int stage = 0;
    private double motionBoost = 0.27;
    private double yPos = -1.0;
    private boolean initialized = false;

    public CubecraftSpeed() {
        super("Cubecraft");
    }

    @Override
    public void onEnable() {
        this.stage = 0;
        this.motionBoost = 0.27;
        this.yPos = -1.0;
        this.initialized = true;
    }

    @Override
    public void onDisable() {
        // restore timer and motion defaults
        try { setTimer(1.0f); } catch (Throwable ignored) {}
        MovementUtils.strafe(0.2);
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        String currentMode = cubecraftMode.value;
        if (!initialized) {
            initialized = true;
            yPos = -1.0;
        }
        switch (currentMode) {
            case "Basic":
                stage++;
                motionBoost = 0.27;
                if (client.player.isOnGround()) {
                    if (autoJump.value) {
                        client.player.jump();
                        event.setY(MovementUtils.getJumpValue());
                        MovementUtils.setMotion(event, motionBoost);
                    }
                    if (stage == 1) {
                        motionBoost = speed.value * 2.4;
                    } else if (stage == 2) {
                        motionBoost = 0.26;
                    } else if (stage >= 3) {
                        stage = 0;
                        motionBoost = 0.26;
                    }
                } else {
                    if (stage == 1) {
                        motionBoost = 0.27;
                        if (event.getY() > 0.0) {
                            motionBoost = 2.0;
                        }
                    } else if (stage > 1) {
                        stage = 0;
                    }
                }
                MovementUtils.setMotion(event, motionBoost);
                break;
            case "Hop":
                if (!client.player.isOnGround() || !MovementUtils.isMoving()) {
                    stage++;
                    if (stage == 1) {
                        motionBoost = 0.4 + MovementUtils.getJumpBoost() * 0.1;
                    }
                    motionBoost -= 0.015;
                    // Omit isBackPressed logic for now
                    motionBoost = Math.max(motionBoost, 0.2);
                    MovementUtils.setMotion(event, motionBoost);
                } else if (autoJump.value) {
                    client.player.jump();
                    event.setY(MovementUtils.getJumpValue());
                    MovementUtils.setMotion(event, motionBoost);
                }
                break;
            case "YPort":
                if (client.player.isOnGround()) {
                    if (MovementUtils.isMoving()) {
                        event.setY(0.53);
                        MovementUtils.setMotion(event, 3.67 * speed.value);
                        stage = 0;
                    }
                    yPos = client.player.getY();
                } else {
                    if (stage == 0 && event.getY() == 0.441) {
                        stage = 1;
                        MovementUtils.setMotion(event, 0.286);
                        event.setY(-0.265);
                        MovementUtils.setPlayerYMotion(event.getY());
                    } else if (stage == 1) {
                        stage = -1;
                        MovementUtils.setMotion(event, 0.285);
                    }
                }
                break;
        }
    }


    @Subscribe
    public void onJump(JumpEvent event) {
        if (cubecraftMode.value.equals("Hop") && autoJump.value) {
            event.velocity = new net.minecraft.util.math.Vec3d(event.velocity.x, MovementUtils.getJumpValue(), event.velocity.z);
            stage = 0;
        }
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (cubecraftMode.value.equals("YPort") && yPos >= 0.0) {
            if (client.player.isOnGround() && WorldUtils.isAboveBounds(client.player, 0.001f)) {
                yPos = client.player.getY();
            }

            client.player.setPosition(client.player.getX(), yPos, client.player.getZ());
            // keep a stable visual Y position like legacy YPort; avoid manipulating internal player fields
            // (previous Rebase set lastTickPosY/prevPosY/cameraYaw — not present in this client version)
        }
    }
}
