package com.skidders.sigma.module

enum class Category {
    GUI("Gui"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    COMBAT("Combat"),
    MISC("Misc"),
    PLAYER("Player"),
    ITEM("Item"),
    WORLD("World");

    val categoryName: String

    constructor(name: String) {
        this.categoryName = name
    }
}
