package io.github.sst.remake.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.util.IMinecraft;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class Module implements IMinecraft {

    public final List<Setting<?>> settings = new ArrayList<>();
    public final String name, description;
    public final Category category;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;

        Client.INSTANCE.moduleManager.currentModule = this;
    }

    public Module(Category category, String name, String description) {
        this(name, description, category);
    }

    public boolean enabled;
    public int keycode = 0;

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onInit() {
    }

    public void toggle() {
        setEnabled(!enabled);

        if (this.enabled) {
            Client.BUS.register(this);
            this.onEnable();
        } else {
            Client.BUS.unregister(this);
            this.onDisable();
        }
    }

    public JsonObject asJson(JsonObject config) throws JsonParseException {
        JsonArray options = config.getAsJsonArray("options");

        this.enabled = config.get("enabled").getAsBoolean();

        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                JsonObject settingCfg = options.get(i).getAsJsonObject();
                String optName = settingCfg.get("name").getAsString();

                for (Setting<?> setting : this.settings) {
                    if (setting.name.equals(optName)) {
                        try {
                            setting.asJson(settingCfg);
                        } catch (JsonParseException jsonException) {
                            System.err.println("Could not initialize settings of " + this.getName() + "." + setting.name + " from config.");
                        }
                        break;
                    }
                }
            }
        }

        if (this.enabled && client.world != null) {
            this.onEnable();
        }

        return config;
    }

    public JsonObject fromJson(JsonObject obj) {
        try {
            obj.addProperty("name", this.getName());
            obj.addProperty("enabled", this.enabled);
            JsonArray jsonArray = new JsonArray();

            for (Setting<?> s : this.settings) {
                jsonArray.add(s.fromJson(new JsonObject()));
            }

            obj.add("options", jsonArray);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
