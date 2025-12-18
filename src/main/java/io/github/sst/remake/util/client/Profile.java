package io.github.sst.remake.util.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;

public class Profile {
    public JsonObject moduleConfig;
    public String name;

    public Profile() {
    }

    public Profile(String name, JsonObject moduleConfig) {
        this.name = name;
        this.moduleConfig = moduleConfig;
    }

    public Profile(String name, Profile profile) {
        this.name = name;
        this.moduleConfig = profile.moduleConfig;
    }

    public Profile loadFromJson(JsonObject jsonObject) throws JsonParseException {
        this.moduleConfig = jsonObject.getAsJsonObject("modConfig");
        this.name = jsonObject.get("name").getAsString();
        return this;
    }

    public JsonObject saveToJson(JsonObject jsonObject) {
        jsonObject.add("modConfig", this.moduleConfig);
        jsonObject.addProperty("name", this.name);
        return jsonObject;
    }

    public JsonObject getDefaultConfig() {
        return null;
    }

    public Profile cloneWithName(String newName) {
        return new Profile(newName, this.moduleConfig);
    }

    public void disableNonGuiModules() throws JsonParseException {
        JsonArray modulesArray = null;

        try {
            modulesArray = this.moduleConfig.getAsJsonArray("mods");
        } catch (JsonParseException ignored) {
        }

        if (modulesArray != null) {
            for (int i = 0; i < modulesArray.size(); i++) {
                JsonObject moduleObject = modulesArray.get(i).getAsJsonObject();
                String moduleName = null;

                try {
                    moduleName = moduleObject.getAsJsonObject("name").getAsString();
                } catch (JsonParseException e) {
                    Client.LOGGER.error("Invalid name in mod list profile", e);
                }

                for (Module module : Client.INSTANCE.moduleManager.modules) {
                    if (module.getName().equals(moduleName) && module.getCategory() != Category.GUI && module.getCategory() != Category.RENDER) {
                        moduleObject.addProperty("enabled", "false");
                    }
                }
            }
        }
    }

    public void updateModuleConfig(JsonObject newConfig, Module module) {
        JsonArray modulesArray = null;

        try {
            modulesArray = this.moduleConfig.getAsJsonArray("mods");
        } catch (JsonParseException ignored) {
        }

        boolean updated = false;
        if (modulesArray != null) {
            for (int i = 0; i < modulesArray.size(); i++) {
                try {
                    JsonObject moduleObject = modulesArray.get(i).getAsJsonObject();
                    String moduleName = moduleObject.get("name").getAsString();

                    if (module.getName().equals(moduleName)) {
                        if (module.getCategory() != Category.GUI && module.getCategory() != Category.RENDER) {
                            modulesArray.add(newConfig);
                        }

                        updated = true;
                    }
                } catch (JsonParseException e) {
                    Client.LOGGER.error("Invalid name in mod list profile", e);
                }

            }
        }

        if (!updated) {
            assert modulesArray != null;
            modulesArray.add(newConfig);
        }
    }

    public JsonObject getModuleConfig(Module module) {
        JsonArray modulesArray = null;

        try {
            modulesArray = this.moduleConfig.getAsJsonArray("mods");
        } catch (JsonParseException ignored) {
        }

        if (modulesArray != null) {
            for (int i = 0; i < modulesArray.size(); i++) {
                JsonObject moduleObject;

                try {
                    moduleObject = modulesArray.get(i).getAsJsonObject();
                } catch (JsonParseException e) {
                    throw new RuntimeException(e);
                }

                String moduleName = null;

                try {
                    moduleName = moduleObject.get("name").getAsString();
                } catch (JsonParseException e) {
                    Client.LOGGER.error("Invalid name in mod list profile", e);
                }

                if (module.getName().equals(moduleName)) {
                    return moduleObject;
                }
            }
        }

        return null;
    }
}
