package io.github.sst.remake.manager.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.io.GsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager extends Manager implements IMinecraft {

    public List<Profile> profiles = new ArrayList<>();
    public Profile currentProfile;

    public boolean guiBlur = false;
    public boolean hqBlur = false;

    @Override
    public void init() {
        if (!ConfigUtils.CLIENT_FOLDER.exists()) {
            ConfigUtils.CLIENT_FOLDER.mkdirs();
        }

        if (!ConfigUtils.PROFILES_FOLDER.exists()) {
            ConfigUtils.PROFILES_FOLDER.mkdirs();
        }

        profiles = ConfigUtils.listAllProfiles();
        loadProfile("Default");
    }

    @Override
    public void shutdown() {
        saveProfile("Default", Client.INSTANCE.moduleManager.getJson());
    }

    public void loadProfile(String name) {
        Profile byName = getProfileByName(name);
        if (byName != null) {
            Client.INSTANCE.moduleManager.loadJson(byName.content);
            currentProfile = byName;
            Client.LOGGER.info("Loaded profile {}", name);
            return;
        }

        Client.LOGGER.info("Profile by the name {} not found", name);
    }

    public void saveProfile(String name, JsonObject content) {
        try {
            String fullName = name.endsWith(ConfigUtils.EXTENSION) ? name : name + ConfigUtils.EXTENSION;
            GsonUtils.save(content, new File(ConfigUtils.PROFILES_FOLDER, fullName));
            Client.LOGGER.info("Saving profile {}", name);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to save profile", e);
        }
    }

    public Profile getProfileByName(String name) {
        return this.profiles.stream()
                .filter(prof -> prof.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getProfileNames() {
        return this.profiles
                .stream()
                .map(prof -> prof.name)
                .collect(Collectors.toList());
    }

}
