package io.github.sst.remake.manager.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.Profile;
import io.github.sst.remake.util.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager extends Manager implements IMinecraft {

    private static final String configFolder = "/profiles/";
    private static final String configFileExtension = ".profile";

    public final File file = new File("sigma5");
    public JsonObject config;

    public boolean guiBlur = false;
    public boolean hqBlur = false;

    public final List<Profile> profiles = new ArrayList<>();
    public Profile profile;

    @Override
    public void init() {
        try {
            if (!file.exists()) {
                file.mkdirs();
            }

            config = FileUtils.readJsonFile(new File(file + "/config.json"));
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load default config", e);
        }
    }

    public void shutdown() {
        try {
            FileUtils.save(config, new File(file + "/config.json"));
        } catch (IOException e) {
            Client.LOGGER.error(e.getMessage());
        }
    }

    public void saveConfig() {
        JsonObject uiConfig = Client.INSTANCE.configManager.config;
        Screen currentScreen = Client.INSTANCE.screenManager.currentScreen;

        if (currentScreen != null) {
            JsonObject json = currentScreen.toConfigWithExtra(new JsonObject());
            if (json.size() != 0) {
                uiConfig.add(currentScreen.getName(), json);
            }
        }

        uiConfig.addProperty("guiBlur", true);
        uiConfig.addProperty("hqIngameBlur", true);
        uiConfig.addProperty("hidpicocoa", true);
    }

    public void loadUIConfig() {
        JsonObject uiConfig = Client.INSTANCE.configManager.config;
        Screen currentScreen = Client.INSTANCE.screenManager.currentScreen;

        if (currentScreen != null) {
            JsonObject json = null;

            try {
                json = Client.INSTANCE.configManager.config.getAsJsonObject(currentScreen.getName());
            } catch (Exception e) {
                json = new JsonObject();
            } finally {
                currentScreen.loadConfig(json);
            }
        }

        if (uiConfig.has("guiBlur")) {
            Client.INSTANCE.configManager.guiBlur = uiConfig.get("guiBlur").getAsBoolean();
        }

        if (uiConfig.has("hqIngameBlur")) {
            Client.INSTANCE.configManager.hqBlur = uiConfig.get("hqIngameBlur").getAsBoolean();
        }
    }

    public void saveProfile(Profile config) {
        try {
            this.profiles.add(0, config);

            File configItself = new File(file + configFolder + config.name + configFileExtension);

            if (configItself.getParentFile() != null) {
                configItself.getParentFile().mkdirs();
            }

            JsonObject jsonConfig = config.saveToJson(new JsonObject());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(new com.google.gson.JsonParser().parse(jsonConfig.toString()));

            Files.write(configItself.toPath(), prettyJson.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + config.name, e);
        }
    }

    public void loadProfile(Profile profile) {
        shutdown();

        if (this.profile == null) {
            return;
        }

        this.profile.moduleConfig = loadCurrentConfig(new JsonObject());
        this.profile = profile;
        config.addProperty("profile", profile.name);

        Client.INSTANCE.moduleManager.load(profile.moduleConfig);
        shutdown();
    }

    public void saveAndReplaceConfigs() throws IOException {
        this.profile.moduleConfig = loadCurrentConfig(new JsonObject());
        File configFolderFolder = new File(file + configFolder);
        if (!configFolderFolder.exists()) {
            configFolderFolder.mkdirs();
        }

        File[] configs = configFolderFolder.listFiles((var0, var1) -> var1.toLowerCase().endsWith(configFileExtension));

        for (File configItself : configs) {
            configItself.delete();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (Profile profile : this.profiles) {
            File configItself = new File(file + configFolder + profile.name + configFileExtension);
            if (!configItself.exists()) {
                configItself.createNewFile();
            }

            String json = gson.toJson(profile.saveToJson(new JsonObject()));

            try (FileOutputStream outputStream = new FileOutputStream(configItself)) {
                IOUtils.write(json, outputStream, "UTF-8");
            }
        }
    }

    private JsonObject loadCurrentConfig(JsonObject obj) {
        JsonArray array = new JsonArray();

        for (Module module : Client.INSTANCE.moduleManager.modules) {
            array.add(module.buildUpModuleData(new JsonObject()));
        }

        obj.add("mods", array);
        return obj;
    }

    public boolean getByName(String name) {
        for (Profile profile : this.profiles) {
            if (profile.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}
