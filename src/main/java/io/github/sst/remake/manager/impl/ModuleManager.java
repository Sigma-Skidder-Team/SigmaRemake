package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.combat.KillAuraModule;
import io.github.sst.remake.module.impl.gui.*;
import io.github.sst.remake.module.impl.misc.TestModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.CorrectMovementModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.module.impl.render.WaypointsModule;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.tracker.impl.RotationTracker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ModuleManager extends Manager {
    public List<Module> modules;

    private int toggleSoundSuppressDepth = 0;

    public RotationTracker rotationTracker;

    @Override
    public void init() {
        rotationTracker = new RotationTracker();
        rotationTracker.enable();

        modules = new ArrayList<>();

        modules.add(new ActiveModsModule());
        modules.add(new BrainFreezeModule());
        modules.add(new WaypointsModule());
        modules.add(new CoordsModule());
        modules.add(new CompassModule());
        modules.add(new MiniMapModule());
        modules.add(new KeyStrokesModule());
        modules.add(new TestModule());
        modules.add(new KillAuraModule());
        modules.add(new CorrectMovementModule());
        modules.add(new BlockFlyModule());
        modules.add(new SafeWalkModule());
        modules.add(new MusicParticlesModule());

        modules.forEach(Module::onInit);
        super.init();
    }

    public Module getModule(String input) {
        return this.modules.stream().filter(m -> m.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public <V extends Module> V getModule(final Class<V> clazz) {
        final Module obj = modules.stream().filter(ob -> ob.getClass().equals(clazz)).findFirst().orElse(null);

        if (obj == null)
            return null;

        return clazz.cast(obj);
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

        toggleSoundSuppressDepth++;
        try {
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
        } finally {
            toggleSoundSuppressDepth = Math.max(0, toggleSoundSuppressDepth - 1);
        }
    }

    public boolean isToggleSoundSuppressed() {
        return toggleSoundSuppressDepth > 0;
    }
}