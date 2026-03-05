package io.github.sst.remake.data.rotation;

import io.github.sst.remake.Client;

public interface Rotatable {
    int getPriority();

    default boolean canPerform() {
        if (!Client.INSTANCE.moduleManager.rotationTracker.isActive()) {
            return false;
        }
        return Client.INSTANCE.moduleManager.rotationTracker.getCurrent() == this;
    }

    boolean isEnabled();

    Rotation getRotations();

    default void registerRotatable() {
        Client.INSTANCE.moduleManager.rotationTracker.addRotatable(this);
    }
}