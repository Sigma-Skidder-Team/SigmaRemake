package info.opensigma.module;

import info.opensigma.OpenSigma;
import info.opensigma.setting.owner.SettingOwner;
import info.opensigma.system.INameable;

public class Module implements INameable {

    protected final SettingOwner settings;

    public final String name, description;
    private boolean enabled;
    public int key;

    public Module(String name, String description, int key) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.key = key;

        this.settings = new SettingOwner(this);

        OpenSigma.getInstance().bindManager.add(() -> key, this::toggle);
    }

    public Module(String name, String description) {
        this(name, description, 0);
    }

    public final void init() {
        this.settings.onMinecraftLoad();
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (enabled)
            enable();
        else
            disable();
    }

    public final void enable() {
        if (this.enabled)
            return;

        onEnable();

        OpenSigma.getInstance().eventBus.subscribe(this);

        enabled = true;
    }

    public final void disable() {
        if (!this.enabled)
            return;

        enabled = false;

        OpenSigma.getInstance().eventBus.subscribe(false);

        onDisable();
    }

    protected void onEnable() { }

    protected void onDisable() { }

    @Override
    public String getName() {
        return name;
    }
}
