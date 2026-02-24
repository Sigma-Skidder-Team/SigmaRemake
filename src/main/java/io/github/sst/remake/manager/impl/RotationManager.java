package io.github.sst.remake.manager.impl;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.render.RenderEntityPitchEvent;
import io.github.sst.remake.event.impl.game.render.RenderEntityYawEvent;
import io.github.sst.remake.event.impl.game.world.EntityLookEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.RotationUtils;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public final class RotationManager extends Manager implements IMinecraft {
    public List<Rotatable> rotatables;

    private float lastSentYaw, lastSentPitch;
    private Rotation rotations;
    private Rotation lastRotations;
    private boolean active;

    @Override
    public void init() {
        super.init();
        rotatables = new ArrayList<>();
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            Rotatable module = rotatables.stream()
                    .filter(Rotatable::isEnabled)
                    .max(Comparator.comparingInt(a -> a.priority))
                    .orElse(null);

            for (Rotatable rotator : rotatables) {
                rotator.canPerform = (rotator == module);
            }

            if (module == null || module.getRotations() == null) {
                active = false;
                lastSentYaw = event.yaw;
                lastSentPitch = event.pitch;
                rotations = null;
                lastRotations = null;
                return;
            }

            Rotation target = module.getRotations();
            if (!active) {
                lastSentYaw = client.player.yaw;
                lastSentPitch = client.player.pitch;
                lastRotations = new Rotation(lastSentYaw, lastSentPitch);
                active = true;
            } else {
                lastRotations = rotations;
            }

            rotations = RotationUtils.applyGcdFix(
                    lastSentYaw, lastSentPitch,
                    target.yaw, target.pitch
            );

            event.yaw = rotations.yaw;
            event.pitch = rotations.pitch;

            lastSentYaw = event.yaw;
            lastSentPitch = event.pitch;

        }
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onLook(EntityLookEvent event) {
        if (rotations == null) return;

        if (event.entity == client.player) {
            event.cancel();
            event.yaw = rotations.yaw;
            event.pitch = rotations.pitch;
        }
    }

    @Subscribe
    public void onRenderYaw(RenderEntityYawEvent event) {
        if (rotations == null || lastRotations == null) return;

        if (event.livingEntity == client.player) {
            event.cancel();
            event.result = MathHelper.lerpAngleDegrees(event.tickDelta, lastRotations.yaw, rotations.yaw);
        }
    }

    @Subscribe
    public void onRenderPitch(RenderEntityPitchEvent event) {
        if (rotations == null || lastRotations == null) return;

        if (event.livingEntity == client.player) {
            event.cancel();
            event.result = MathHelper.lerp(event.tickDelta, lastRotations.pitch, rotations.pitch);
        }
    }
}