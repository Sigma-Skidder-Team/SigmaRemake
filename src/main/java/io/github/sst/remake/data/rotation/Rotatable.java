package io.github.sst.remake.data.rotation;

import io.github.sst.remake.Client;

public interface Rotatable {
    int getPriority();

    default boolean canPerform() {
        if (Client.INSTANCE == null || Client.INSTANCE.moduleManager == null
                || Client.INSTANCE.moduleManager.rotationTracker == null) {
            return false;
        }
        return Client.INSTANCE.moduleManager.rotationTracker.getCurrentRotatable() == this;
    }

    boolean isEnabled();

    Rotation getRotations();

    default void registerRotatable() {
        Client.INSTANCE.moduleManager.rotationTracker.rotatables.add(this);
    }
}