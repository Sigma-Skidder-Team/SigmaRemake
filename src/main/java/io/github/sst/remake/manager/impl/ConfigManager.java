package io.github.sst.remake.manager.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ConfigManager extends Manager implements IMinecraft {

    public final File file = new File("sigma5");
    public JsonObject config;

    public boolean guiBlur = false;
    public boolean hqBlur = false;

    public String profile = "Default";

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
}
