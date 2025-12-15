package io.github.sst.remake.util.client.bind;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.client.ScreenUtils;
import net.minecraft.client.gui.screen.Screen;

public class Bound {
    private int key = -1;
    private Object target;

    public Bound(JsonObject json) {
        this.loadFromJSON(json);
    }

    public Bound(int key, Module target) {
        this.key = key;
        this.target = target;
    }

    public Bound(int key, Class<? extends Screen> target) {
        this.key = key;
        this.target = target;
    }

    public void loadFromJSON(JsonObject from) {
        if (from.has("target")) {
            try {
                if (from.has("key")) {
                    this.key = from.get("key").getAsInt();
                }

                if (from.has("type")) {
                    String var4 = from.get("type").getAsString();
                    switch (var4) {
                        case "mod":
                            for (Module module : Client.INSTANCE.moduleManager.modules) {
                                if (from.get("target").getAsString().equals(module.getName())) {
                                    this.target = module;
                                }
                            }
                        case "screen":
                            Class screen = ScreenUtils.getScreenByName(from.get("target").getAsString());
                            if (screen != null) {
                                this.target = screen;
                            }
                    }
                }
            } catch (JsonParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JsonObject getKeybindData() {
        JsonObject obj = new JsonObject();
        switch (this.getKeybindTypes()) {
            case MODULE:
                obj.addProperty("type", "mod");
                obj.addProperty("target", ((Module) this.target).getName());
                break;
            case SCREEN:
                obj.addProperty("type", "screen");
                obj.addProperty("target", ScreenUtils.getNameForTarget((Class<? extends Screen>) this.target));
        }

        obj.addProperty("key", this.key);
        return obj;
    }

    public boolean hasTarget() {
        return this.target != null;
    }

    public int getKeybind() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public KeybindTypes getKeybindTypes() {
        return !(this.target instanceof Module) ? KeybindTypes.SCREEN : KeybindTypes.MODULE;
    }

    public Object getTarget() {
        return this.target;
    }

    public Class<? extends Screen> getScreenTarget() {
        return (Class<? extends Screen>) this.target;
    }

    public Module getModuleTarget() {
        return (Module) this.target;
    }
}
