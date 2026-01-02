package io.github.sst.remake.manager.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.io.FileUtils;
import io.github.sst.remake.util.io.GsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager extends Manager implements IMinecraft {

    public List<Profile> profiles = new ArrayList<>();
    public Profile currentProfile;

    public boolean guiBlur = true;
    public boolean hqBlur = true;

    @Override
    public void init() {
        if (!ConfigUtils.CLIENT_FOLDER.exists()) {
            ConfigUtils.CLIENT_FOLDER.mkdirs();
        }

        if (!ConfigUtils.PROFILES_FOLDER.exists()) {
            ConfigUtils.PROFILES_FOLDER.mkdirs();
        }

        profiles = ConfigUtils.listAllProfiles();
        loadClientConfig();
        loadProfile("Default");
    }

    @Override
    public void shutdown() {
        saveClientConfig();
        saveProfile("Default", Client.INSTANCE.moduleManager.getJson(), false);
    }

    public void renameProfile(Profile from, String to) {
        String oldName = from.name;
        if (ConfigUtils.renameProfile(from, to)) {
            Client.LOGGER.info("Renamed profile '{}' to '{}'", oldName, to);
        }
    }

    public void deleteProfile(Profile profile) {
        if (profiles.contains(profile)) {
            profiles.remove(profile);
            if (ConfigUtils.deleteProfile(profile)) {
                loadProfile("Default");
            }
            Client.LOGGER.info("Removed & deleted profile '{}'", profile.name);
        } else {
            Client.LOGGER.info("Profile '{}' doesn't exist anymore", profile.name);
        }
    }

    public void loadProfile(String name) {
        Profile byName = getProfileByName(name);
        if (byName != null) {
            loadProfile(byName);
            return;
        }

        Client.LOGGER.info("Profile by the name '{}' not found", name);
    }

    public void loadProfile(Profile profile) {
        Client.INSTANCE.moduleManager.loadJson(profile.content);
        currentProfile = profile;
        Client.LOGGER.info("Loaded profile '{}'", profile.name);
    }

    public void saveProfile(String name, JsonObject content, boolean add) {
        saveProfile(new Profile(name, content), add);
    }

    public void saveProfile(Profile profile, boolean add) {
        try {
            String fullName = profile.name.endsWith(ConfigUtils.EXTENSION) ? profile.name : profile.name + ConfigUtils.EXTENSION;
            GsonUtils.save(profile.content, new File(ConfigUtils.PROFILES_FOLDER, fullName));

            if (add) {
                profiles.add(profile);
            }

            Client.LOGGER.info("{} profile '{}'", add ? "Adding" : "Saving", profile.name);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to save profile", e);
        }
    }

    public void saveClientConfig() {
        JsonObject object = new JsonObject();
        object.addProperty("GUI Blur", guiBlur);
        object.addProperty("GPU Acceleration", hqBlur);

        JsonObject bindsObject = new JsonObject();
        Client.INSTANCE.bindManager.save(bindsObject);
        object.add("Binds", bindsObject);

        try {
            GsonUtils.save(object, ConfigUtils.CONFIG_FILE);
            Client.LOGGER.info("Saved client configuration");
        } catch (IOException e) {
            Client.LOGGER.error("Failed to save client configuration", e);
        }
    }

    public void loadClientConfig() {
        if (!ConfigUtils.CONFIG_FILE.exists()) {
            Client.LOGGER.warn("Client configuration file doesn't exist");
            return;
        }

        JsonObject object = JsonParser.parseString(FileUtils.readFile(ConfigUtils.CONFIG_FILE)).getAsJsonObject();

        if (object.isEmpty()) {
            Client.LOGGER.warn("Client configuration file is empty");
            return;
        }

        if (object.has("GUI Blur")) {
            guiBlur = object.get("GUI Blur").getAsBoolean();
        }

        if (object.has("GPU Acceleration")) {
            guiBlur = object.get("GPU Acceleration").getAsBoolean();
        }

        if (object.has("Binds")) {
            Client.INSTANCE.bindManager.load(object.get("Binds").getAsJsonObject());
        }

        Client.LOGGER.info("Loaded client configuration");
    }

    public Profile getProfileByName(String name) {
        return this.profiles.stream()
                .filter(prof -> prof.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean doesProfileExist(String name) {
        for (Profile config : this.profiles) {
            if (config.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public List<String> getProfileNames() {
        return this.profiles
                .stream()
                .map(prof -> prof.name)
                .collect(Collectors.toList());
    }

}
