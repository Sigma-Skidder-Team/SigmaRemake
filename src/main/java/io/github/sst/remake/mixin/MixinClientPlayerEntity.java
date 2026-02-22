package io.github.sst.remake.mixin;

import com.mojang.authlib.GameProfile;
import io.github.sst.remake.data.bus.State;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Unique
    private ClientPlayerTickEvent clientPlayerTickEvent;

    @Unique
    private MotionEvent motionEvent;

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

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void injectSendMovementPackets(CallbackInfo ci) {
        motionEvent = new MotionEvent(getX(), getY(), getZ(), yaw, pitch, onGround);
        motionEvent.call();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D"))
    private double redirectGetX(ClientPlayerEntity instance) {
        return motionEvent.x;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D"))
    private double redirectGetY(ClientPlayerEntity instance) {
        return motionEvent.y;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D"))
    private double redirectGetZ(ClientPlayerEntity instance) {
        return motionEvent.z;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;onGround:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectOnGround(ClientPlayerEntity instance) {
        return motionEvent.onGround;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;yaw:F", opcode = Opcodes.GETFIELD))
    private float redirectYaw(ClientPlayerEntity instance) {
        return motionEvent.yaw;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;pitch:F", opcode = Opcodes.GETFIELD))
    private float redirectPitch(ClientPlayerEntity instance) {
        return motionEvent.pitch;
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void injectSendMovementPacketsEnd(CallbackInfo ci) {
        motionEvent.state = State.POST;
        motionEvent.call();
    }

}
