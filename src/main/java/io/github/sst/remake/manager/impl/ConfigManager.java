package io.github.sst.remake.manager.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ConfigManager extends Manager implements IMinecraft {

    public final File file = new File("sigma5");
    public JsonObject config;

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
}
