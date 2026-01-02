package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.gui.BrainFreezeModule;
import io.github.sst.remake.module.impl.gui.CoordsModule;
import io.github.sst.remake.module.impl.misc.TestModule;
import io.github.sst.remake.module.impl.render.WaypointsModule;
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
        modules.add(new WaypointsModule());
        modules.add(new CoordsModule());
        initModules();
        super.init();
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

    public JsonObject getJson() {
        JsonObject profileObject = new JsonObject();
        JsonArray modsArray = new JsonArray();

        for (Module mod : modules) {
            JsonObject moduleObject = new JsonObject();
            JsonArray optionsArray = new JsonArray();

            for (Setting<?> setting : mod.settings) {
                JsonObject settingJson = new JsonObject();
                optionsArray.add(setting.fromJson(settingJson));
            }

            moduleObject.addProperty("name", mod.name);
            moduleObject.add("options", optionsArray);
            moduleObject.addProperty("enabled", mod.enabled);

            modsArray.add(moduleObject);
        }

        profileObject.add("mods", modsArray);
        return profileObject;
    }

    public void loadJson(JsonObject profileObject) {
        if (!profileObject.has("mods")) {
            return;
        }

        JsonArray modsArray = profileObject.getAsJsonArray("mods");

        for (JsonElement modElement : modsArray) {
            JsonObject modObject = modElement.getAsJsonObject();
            String modName = modObject.get("name").getAsString();

            Module mod = null;
            for (Module m : modules) {
                if (m.name.equalsIgnoreCase(modName)) {
                    mod = m;
                    break;
                }
            }

            if (mod == null) {
                continue;
            }

            mod.setEnabled(false);

            if (!modObject.has("options")) {
                continue;
            }

            JsonArray optionsArray = modObject.getAsJsonArray("options");

            for (JsonElement optionElement : optionsArray) {
                JsonObject optionObject = optionElement.getAsJsonObject();
                String settingName = optionObject.get("name").getAsString();

                for (Setting<?> setting : mod.settings) {
                    if (!setting.name.equalsIgnoreCase(settingName)) {
                        continue;
                    }

                    if (!optionObject.has("value")) {
                        break;
                    }

                    setting.loadFromJson(optionObject.get("value"));
                    break;
                }
            }

            if (modObject.has("enabled")) {
                mod.setEnabled(modObject.get("enabled").getAsBoolean());
            }
        }
    }
}
