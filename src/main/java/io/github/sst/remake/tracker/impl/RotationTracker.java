package io.github.sst.remake.tracker.impl;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.render.RenderEntityRotationsEvent;
import io.github.sst.remake.event.impl.game.world.EntityLookEvent;
import io.github.sst.remake.tracker.Tracker;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.RotationUtils;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public final class RotationTracker extends Tracker implements IMinecraft {
    public List<Rotatable> rotatables;
    private Rotatable currentRotatable;

    public Rotation rotations;

    private Rotation renderHeadPrevious;
    private Rotation renderHeadCurrent;

    private float renderBodyPrevious;
    private float renderBodyCurrent;

    private static final float BODY_TURN_SPEED = 30.0f;

    public boolean active;

    @Override
    public void enable() {
        rotatables = new ArrayList<>();
        currentRotatable = null;
        rotations = null;

        renderHeadPrevious = null;
        renderHeadCurrent = null;

        renderBodyPrevious = 0.0f;
        renderBodyCurrent = 0.0f;

        active = false;
        super.enable();
    }

    @SuppressWarnings("DataFlowIssue")
    @Subscribe(priority = Priority.HIGHEST)
    public void onMotion(MotionEvent event) {
        if (!event.isPre()) return;

        Rotatable module = rotatables.stream()
                .filter(Rotatable::isEnabled)
                .max(Comparator.comparingInt(Rotatable::getPriority))
                .orElse(null);
        currentRotatable = module;

        Rotation target = module == null ? null : module.getRotations();
        if (target == null) {
            active = false;
            currentRotatable = null;
            rotations = null;

            renderHeadPrevious = null;
            renderHeadCurrent = null;

            return;
        }

        if (!active || renderHeadCurrent == null) {
            float currentYaw = client.player.yaw;
            float currentPitch = client.player.pitch;

            renderHeadPrevious = new Rotation(currentYaw, currentPitch);
            renderHeadCurrent = new Rotation(currentYaw, currentPitch);

            renderBodyPrevious = currentYaw;
            renderBodyCurrent = currentYaw;

            active = true;
        }

        renderHeadPrevious = renderHeadCurrent;
        renderBodyPrevious = renderBodyCurrent;

        renderHeadCurrent = target;

        renderBodyCurrent = approachAngle(
                renderBodyPrevious,
                renderHeadCurrent.yaw
        );

        rotations = RotationUtils.applyGcdFix(
                renderHeadPrevious.yaw, renderHeadPrevious.pitch,
                renderHeadCurrent.yaw, renderHeadCurrent.pitch
        );

        event.yaw = rotations.yaw;
        event.pitch = rotations.pitch;
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

    @Subscribe(priority = Priority.HIGHEST)
    public void onRenderYaw(RenderEntityRotationsEvent event) {
        if (renderHeadCurrent == null || renderHeadPrevious == null) return;
        if (event.livingEntity != client.player) return;

        float headYaw = MathHelper.lerpAngleDegrees(
                event.tickDelta,
                renderHeadPrevious.yaw,
                renderHeadCurrent.yaw
        );

        float bodyYaw = MathHelper.lerpAngleDegrees(
                event.tickDelta,
                renderBodyPrevious,
                renderBodyCurrent
        );

        float pitch = MathHelper.lerp(
                event.tickDelta,
                renderHeadPrevious.pitch,
                renderHeadCurrent.pitch
        );

        event.bodyYaw = bodyYaw;
        event.headYaw = headYaw;
        event.pitch = pitch;

        event.yaw = MathHelper.wrapDegrees(headYaw - bodyYaw);
    }

    private static float approachAngle(float from, float to) {
        float delta = MathHelper.wrapDegrees(to - from);

        if (delta > BODY_TURN_SPEED) delta = BODY_TURN_SPEED;
        if (delta < -BODY_TURN_SPEED) delta = -BODY_TURN_SPEED;

        return from + delta;
    }

    public Rotatable getCurrentRotatable() {
        return currentRotatable;
    }
}