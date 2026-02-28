package io.github.sst.remake.module;

public enum Category {
    RENDER("Render"),
    PLAYER("Player"),
    COMBAT("Combat"),
    WORLD("World"),
    MISC("Misc"),
    EXPLOIT("Exploit"),
    MOVEMENT("Movement"),
    GUI("Gui"),
    ITEM("Item");

    public final String name;

    Category(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}