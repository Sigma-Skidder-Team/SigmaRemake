package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.util.game.world.WorldUtils;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class AACSpeed extends SubModule {
    public final ModeSetting aacMode = new ModeSetting("Mode", "AAC mode", 0, "Basic", "Fast1", "Fast2");
    public final BooleanSetting fluidFix = new BooleanSetting("Fluid Fix", "Makes your jump fluid.", true);
    public final BooleanSetting autoJump = new BooleanSetting("Auto Jump", "Automatically jumps for you.", true);

    private int jumpStage = -1;
    private int speedStage = 0;
    private int aboveBlockCounter = 0;
    private double storedY = 0.0;
    private double internalSpeed = 0.0;
    private double verticalMotionTemp = 0.0;

    public AACSpeed() {
        super("AAC");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        jumpStage = -1;
        speedStage = 0;
        aboveBlockCounter = 0;
        storedY = -1.0;
        internalSpeed = 0.0;
        verticalMotionTemp = 0.0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setTimer(1.0f);
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            this.speedStage = 0;
            this.jumpStage = -1;
            this.aboveBlockCounter = 0;
        }
    }
    @Subscribe
    public void onMove(MoveEvent event) {
        String selectedMode = aacMode.value;
        if (autoJump.value && MovementUtils.isMoving() && client.player.isOnGround()) {
            client.player.jump();
            event.setY(MovementUtils.getJumpValue());
        }
        // Full AAC parity: handle Basic, Fast1 and Fast2 modes
        // Handle above-bounds (fluid-like) state similar to Rebase
        if (WorldUtils.isAboveBounds(client.player, 0.01f)) {
            if (this.aboveBlockCounter <= 1) {
                this.aboveBlockCounter++;
            } else {
                this.speedStage = 0;
                this.jumpStage = -1;
            }
            if (MovementUtils.isMoving() && autoJump.value) {
                client.player.jump();
                event.setY(client.player.getVelocity().y);
            }
        } else if (this.jumpStage >= 0) {
            this.jumpStage++;
        }

        if (!MovementUtils.isMoving() || client.player.horizontalCollision || client.player.forwardSpeed <= 0.0F) {
            this.speedStage = 0;
        }

        double motion = 0.0;
        double vMotion = event.getY();

        switch (selectedMode) {
            case "Basic":
                motion = MovementUtils.getAacHopSpeed(jumpStage, speedStage, () -> speedStage = 0);
                break;
            case "Fast1":
                motion = this.method16013(this.jumpStage, this.speedStage);
                vMotion = this.method16012(this.jumpStage);
                event.setY(vMotion);
                MovementUtils.setPlayerYMotion(vMotion);
                break;
            case "Fast2":
                motion = this.method16015(this.jumpStage, this.speedStage);
                vMotion = this.method16014(this.jumpStage);
                event.setY(vMotion);
                MovementUtils.setPlayerYMotion(vMotion);
                break;
        }

        if (!MovementUtils.isMoving()) motion = 0.0;

        if (client.player.horizontalCollision) {
            motion = motion * 0.9 < 0.27 ? 0.27 : motion * 0.9;
        }

        if (this.jumpStage >= 0) {
            MovementUtils.setMotion(event, motion);
        }

        // ensure player's motion Y matches event
        MovementUtils.setPlayerYMotion(event.getY());
    }


    @Subscribe
    public void onJump(JumpEvent event) {
        this.jumpStage = 0;
            this.aboveBlockCounter = 0;
        // update speedStage counts based on mode
        String selectedMode = aacMode.value;
        switch (selectedMode) {
            case "Basic":
                if (this.speedStage < 3) this.speedStage++;
                break;
            case "Fast1":
                if (this.speedStage < 5) this.speedStage++;
                if (this.jumpStage < 11 && this.jumpStage > 0) this.speedStage = 0;
                break;
            case "Fast2":
                if (this.speedStage < 4) this.speedStage++;
                break;
        }
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!fluidFix.value) return;
        if (!aacMode.value.equals("Basic") && !aacMode.value.equals("Fast1") && !aacMode.value.equals("Fast2")) return;

        if (!client.player.horizontalCollision && !client.player.verticalCollision) {
            String m = aacMode.value;
            float limit = 13.0f;
            if (m.equals("Fast1")) limit = 11.0f;
            if (!(this.jumpStage > limit) && this.jumpStage >= 0) {
                double cos = Math.cos(Math.toRadians((float) this.jumpStage / limit * 180.0F - 90.0F));
                client.player.setPosition(client.player.getX(), this.storedY + cos, client.player.getZ());
                client.player.prevHorizontalSpeed = 0; // best-effort to keep camera small; no exact analogue
            }
        } else {
            client.player.setPosition(client.player.getX(), client.player.getBoundingBox().minY, client.player.getZ());
            this.storedY = client.player.getY();
            this.jumpStage = -1;
        }
    }

    private double method16012(int var1) {
        double var4 = client.player.getVelocity().y;
        boolean var6 = WorldUtils.isAboveBounds(client.player, 0.37f);
        double[] var7 = new double[]{0.41, 0.309, 0.21, 0.113, 0.03, -0.05, -0.12, -0.192, -0.26, -0.33, !var6 ? -0.4 : -0.0, !var6 ? -0.47 : -0.13};
        if (var1 >= 0 && var1 < var7.length) {
            var4 = var7[var1];
        }
        return var4;
    }

    private double method16013(int var1, int var2) {
        boolean var5 = WorldUtils.isAboveBounds(client.player, 0.37f);
        double[] var6 = new double[]{0.497, 0.671, 0.719, 0.733, 0.738};
        double[] var7 = new double[]{0.303, 0.407, 0.436, 0.444, 0.447};
        double[] var8 = new double[]{0.0, 0.003, 0.004, 0.004, 0.004};
        double[] var9 = new double[]{0.605, 0.685, 0.708, 0.716, 0.719};
        double[] var10 = new double[]{0.367, 0.415, 0.429, 0.434, 0.434};
        if (var1 >= 0 && var2 >= 1 && var2 <= 5) {
            if (var1 != 0) {
                if (var1 != 1) {
                    if (var1 == 10 && var5) {
                        this.internalSpeed = var9[var2 - 1];
                    } else if (var1 == 11 && var5) {
                        this.internalSpeed = var10[var2 - 1];
                    } else {
                        this.internalSpeed = this.internalSpeed - var8[var2 - 1];
                    }
                } else {
                        this.internalSpeed = var7[var2 - 1];
                }
            } else {
                        this.internalSpeed = var6[var2 - 1];
            }

            if (var2 == 1 && var1 == 2) {
                this.internalSpeed -= 0.002;
            }

            if (var2 == 2 && (var1 == 2 || var1 == 3)) {
                this.internalSpeed -= 0.001;
            }
        }

        return this.internalSpeed;
    }

    private double method16014(int var1) {
        double var4 = client.player.getVelocity().y;
        boolean var6 = WorldUtils.isAboveBounds(client.player, 0.37f);
        double[] var7 = new double[]{0.41, 0.309, 0.21, 0.113, 0.03, -0.06, -0.14, -0.22, -0.29, 0.0, -0.082, -0.11, 0.0, -0.18};
        if (var1 >= 0 && var1 < var7.length) {
            var4 = var7[var1];
        }

        if (var1 >= 9 && var1 <= 11 && !var6) {
            var4 -= 0.36;
        }

        if (var1 >= 12 && var1 <= 13 && !var6) {
            var4 -= 0.5;
        }

        return var4;
    }

    private double method16015(int var1, int var2) {
        boolean var5 = WorldUtils.isAboveBounds(client.player, 0.37f);
        double[] var6 = new double[]{0.497, 0.709, 0.746, 0.753};
        double[] var7 = new double[]{0.303, 0.43, 0.4525, 0.456};
        double[] var8 = new double[]{0.0, 0.0036, 0.0041, 0.0042};
        double[] var9 = new double[]{0.605, 0.707, 0.728, 0.731};
        double[] var10 = new double[]{0.367, 0.429, 0.441, 0.443};
        double[] var11 = new double[]{0.668, 0.73, 0.741, 0.743};
        double[] var12 = new double[]{0.405, 0.442, 0.449, 0.45};
        if (var1 >= 0 && var2 >= 1 && var2 <= 4) {
            if (var1 != 0) {
                if (var1 != 1) {
                    if (var1 == 9 && var5) {
                        this.internalSpeed = var9[var2 - 1];
                    } else if (var1 == 10 && var5) {
                        this.internalSpeed = var10[var2 - 1];
                    } else if (var1 == 12 && var5) {
                        this.internalSpeed = var11[var2 - 1];
                    } else if (var1 == 13 && var5) {
                        this.internalSpeed = var12[var2 - 1];
                    } else {
                        this.internalSpeed = this.internalSpeed - var8[var2 - 1];
                    }
                } else {
                    this.internalSpeed = var7[var2 - 1];
                }
            } else {
                this.internalSpeed = var6[var2 - 1];
            }

                if (var2 == 1) {
                if (var1 != 2) {
                    if (var1 == 11) {
                        this.internalSpeed -= 0.003;
                    }
                } else {
                    this.internalSpeed -= 0.002;
                }
            }

            if (var2 == 2 && (var1 == 2 || var1 == 3)) {
                this.internalSpeed -= 0.001;
            }
        }

        return this.internalSpeed;
    }
}
