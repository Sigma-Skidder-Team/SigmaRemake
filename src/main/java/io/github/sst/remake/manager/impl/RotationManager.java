package io.github.sst.remake.manager.impl;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.world.EntityLookEvent;
import io.github.sst.remake.manager.Manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public class RotationManager extends Manager {

    public final List<Rotatable> rotatables = new ArrayList<>();
    private Rotatable module;
    private Rotation rotations;

    @Subscribe(priority = Priority.HIGHEST)
    public void onMotion(MotionEvent event) {
        module = rotatables.stream()
                .filter(Rotatable::isEnabled)
                .max(Comparator.comparingInt(a -> a.priority))
                .orElse(null);

        for (Rotatable rotator : rotatables) {
            rotator.canPerform = (rotator == module);
        }

        if (module == null) {
            rotations = null;
            return;
        }

        rotations = module.getRotations();
        if (rotations == null) return;

        event.yaw = rotations.yaw;
        event.pitch = rotations.pitch;
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onLook(EntityLookEvent event) {
        if (rotations == null) return;

        event.yaw = rotations.yaw;
        event.pitch = rotations.pitch;
    }

}