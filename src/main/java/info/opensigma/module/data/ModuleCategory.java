package info.opensigma.module.data;

public enum ModuleCategory {
    GUI("Gui"),
    COMBAT("Combat"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    PLAYER("Player"),
    ITEM("Item"),
    MOVEMENT("Movement");

    public final String name;

    ModuleCategory(String name) {
        this.name = name;
    }
}
