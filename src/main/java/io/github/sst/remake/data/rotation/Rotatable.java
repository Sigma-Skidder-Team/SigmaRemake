package io.github.sst.remake.data.rotation;

import io.github.sst.remake.Client;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;

public abstract class Rotatable extends Module {

    public final int priority;
    public boolean canPerform;

    public Rotatable(String name, String description, Category category, int priority) {
        super(name, description, category);
        this.priority = priority;

        Client.INSTANCE.rotationManager.rotatables.add(this);
    }

    public abstract Rotation getRotations();

}
