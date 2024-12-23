package com.skidders.sigma.module;

public enum Category {
    GUI("Gui"),
    COMBAT("Combat"),
    RENDER("Render"),
    WORLD("World"),
    PLAYER("Player"),
    ITEM("Item"),
    MOVEMENT("Movement"),
    MISC("Misc");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}
