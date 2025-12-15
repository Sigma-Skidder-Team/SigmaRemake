package io.github.sst.remake.module;

public enum Category {
    RENDER,
    PLAYER,
    COMBAT,
    WORLD,
    MISC,
    EXPLOIT,
    MOVEMENT,
    GUI,
    ITEM;

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
