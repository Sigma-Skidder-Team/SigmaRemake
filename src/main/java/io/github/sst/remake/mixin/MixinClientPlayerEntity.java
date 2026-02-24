package io.github.sst.remake.mixin;

import com.mojang.authlib.GameProfile;
import io.github.sst.remake.data.bus.State;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    @Shadow
    private boolean lastSprinting;
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    private boolean lastSneaking;

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    private double lastX;
    @Shadow
    private double lastBaseY;
    @Shadow
    private double lastZ;
    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;
    @Shadow
    private int ticksSinceLastPositionPacketSent;
    @Shadow
    private boolean lastOnGround;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    @Final
    protected MinecraftClient client;
    @Unique
    private ClientPlayerTickEvent clientPlayerTickEvent;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(CallbackInfo ci) {
        clientPlayerTickEvent = new ClientPlayerTickEvent();
        clientPlayerTickEvent.call();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void injectTickEnd(CallbackInfo ci) {
        clientPlayerTickEvent.state = State.POST;
        clientPlayerTickEvent.call();
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void injectSendMovementPackets(CallbackInfo ci) {
        ci.cancel();

        MotionEvent motionEvent = new MotionEvent(getX(), getY(), getZ(), yaw, pitch, onGround);
        motionEvent.call();

        boolean sneaking = this.isSneaking();
        boolean sprinting = this.isSprinting();

        if (sprinting != this.lastSprinting) {
            ClientCommandC2SPacket.Mode mode = sprinting ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSprinting = sprinting;
        }

        if (sneaking != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode = sneaking ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSneaking = sneaking;
        }

        if (this.isCamera()) {
            double dX = motionEvent.x - this.lastX;
            double dY = motionEvent.y - this.lastBaseY;
            double dZ = motionEvent.z - this.lastZ;
            double dYaw = motionEvent.yaw - this.lastYaw;
            double dPitch = motionEvent.pitch - this.lastPitch;

            ++this.ticksSinceLastPositionPacketSent;

            boolean moving = dX * dX + dY * dY + dZ * dZ > 9.0E-4 || this.ticksSinceLastPositionPacketSent >= 20;
            boolean looking = dYaw != 0.0 || dPitch != 0.0;

            if (this.hasVehicle()) {
                Vec3d vec3d = this.getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Both(vec3d.x, -999.0, vec3d.z, motionEvent.yaw, motionEvent.pitch, motionEvent.onGround));
                moving = false;
            } else if (moving && looking) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Both(motionEvent.x, motionEvent.y, motionEvent.z, motionEvent.yaw, motionEvent.pitch, motionEvent.onGround));
            } else if (moving) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(motionEvent.x, motionEvent.y, motionEvent.z, motionEvent.onGround));
            } else if (looking) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(motionEvent.yaw, motionEvent.pitch, motionEvent.onGround));
            } else if (this.lastOnGround != motionEvent.onGround) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket(motionEvent.onGround));
            }

            if (moving) {
                this.lastX = motionEvent.x;
                this.lastBaseY = motionEvent.y;
                this.lastZ = motionEvent.z;
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (looking) {
                this.lastYaw = motionEvent.yaw;
                this.lastPitch = motionEvent.pitch;
            }

            this.lastOnGround = motionEvent.onGround;
            this.autoJumpEnabled = this.client.options.autoJump;
        }

        motionEvent.state = State.POST;
        motionEvent.call();
    }
}