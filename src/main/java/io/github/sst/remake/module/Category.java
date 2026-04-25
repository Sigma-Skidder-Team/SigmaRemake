package io.github.sst.remake.module;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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

    @Override
    public String toString() {
        return name;
    }
}