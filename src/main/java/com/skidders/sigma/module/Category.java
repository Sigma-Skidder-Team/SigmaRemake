package com.skidders.sigma.module;

public enum Category {
    GUI("Gui"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    COMBAT("Combat"),
    MISC("Misc"),
    PLAYER("Player"),
    ITEM("Item"),
    WORLD("World");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}
