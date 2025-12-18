package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.BrainFreezeModule;
import io.github.sst.remake.module.impl.TestModule;
import io.github.sst.remake.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager extends Manager {

    public final List<Module> modules = new ArrayList<>();

    public Module currentModule = null;

    @Override
    public void init() {
        modules.add(new TestModule());
        modules.add(new BrainFreezeModule());
        initModules();
        super.init();
    }

    @Override
    public void shutdown() {
        modules.clear();
        super.shutdown();
    }

    private void initModules() {
        modules.forEach(Module::onInit);
    }

    public Module getModule(String input) {
        return this.modules.stream().filter(m -> m.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(Category input) {
        return this.modules.stream().filter(mod ->
                mod.category.equals(input)).collect(Collectors.toList()
        );
    }

    public JsonObject load(JsonObject json) {
        JsonArray modsArray = null;

        try {
            modsArray = json.getAsJsonArray("mods");
        } catch (JsonParseException e) {
            Client.LOGGER.error("Failed to parse mods from profile", e);
        }

        for (Module module : this.modules) {
            if (module.enabled) {
                module.onDisable();
            }

            module.enabled = false;

            for (Setting setting : module.settings) {
                setting.value = setting.defaultValue;
            }
        }

        if (modsArray == null) {
            Client.LOGGER.warn("Mods modsArray does not exist in config. Assuming a blank profile...");
            return json;
        }

        for (int i = 0; i < modsArray.size(); i++) {
            JsonObject moduleObject;
            try {
                moduleObject = modsArray.get(i).getAsJsonObject();
            } catch (JsonParseException e) {
                throw new JsonParseException("Failed to parse module in array", e);
            }

            String moduleName = null;

            try {
                moduleName = moduleObject.get("name").getAsString();
            } catch (JsonParseException e) {
                throw new JsonParseException("Failed to parse module in array", e);
            }

            for (Module module : this.modules) {
                if (module.getName().equals(moduleName)) {
                    try {
                        module.initialize(moduleObject);
                    } catch (JsonParseException e) {
                        Client.LOGGER.warn("Could not initialize mod {} from config", module.getName(), e);
                    }
                    break;
                }
            }
        }

        for (Module module : this.modules) {
            if (module.isEnabled()) {
                Client.BUS.register(this);
            } else {
                Client.BUS.unregister(this);
            }

            module.onInit();
        }

        return json;
    }
}
