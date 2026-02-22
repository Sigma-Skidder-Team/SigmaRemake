package io.github.sst.remake.manager.impl;

import com.google.gson.*;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.data.profile.Profile;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.io.FileUtils;
import io.github.sst.remake.util.io.GsonUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager extends Manager implements IMinecraft {

    public List<Profile> profiles = new ArrayList<>();
    public Profile currentProfile;

    public boolean guiBlur = true;
    public boolean hqBlur = true;

    public JsonObject screenConfig = new JsonObject();
    private boolean hasLoadedScreens = false;

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
        loadAlts();
        loadProfile("Default");
        loadScreenConfig();
    }

    @Override
    public void shutdown() {
        saveClientConfig();
        saveProfile("Default", Client.INSTANCE.moduleManager.getJson(), false);
        saveAlts();
        saveScreenConfig(true);
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
        if (profile == null) {
            Client.LOGGER.error("No loaded profile found.");
            return;
        }

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

        JsonObject object = new JsonParser().parse(FileUtils.readFile(ConfigUtils.CONFIG_FILE)).getAsJsonObject();

        if (object.size() == 0) {
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

    public void saveScreenConfig(boolean hard) {
        Screen currentScreen = Client.INSTANCE.screenManager.currentScreen;
        if (currentScreen != null) {
            JsonObject currentScreenConfig = currentScreen.toConfigWithExtra(new JsonObject());
            if (currentScreenConfig.size() != 0) {
                this.screenConfig.add(currentScreen.getName(), currentScreenConfig);
            }
        }

        if (hard) {
            try {
                GsonUtils.save(this.screenConfig, ConfigUtils.SCREENS_FILE);
            } catch (IOException e) {
                Client.LOGGER.error("Failed to save screen configuration", e);
            }
        }
    }

    public void loadScreenConfig() {
        if (!hasLoadedScreens) {
            if (ConfigUtils.SCREENS_FILE.exists()) {
                try {
                    String fileContent = FileUtils.readFile(ConfigUtils.SCREENS_FILE);
                    if (!fileContent.trim().isEmpty()) {
                        this.screenConfig = new JsonParser().parse(fileContent).getAsJsonObject();
                        Client.LOGGER.info("Loaded screen configuration");
                    }
                } catch (Exception e) {
                    Client.LOGGER.error("Failed to parse screen configuration", e);
                    this.screenConfig = new JsonObject();
                }
            }
            hasLoadedScreens = true;
        }

        Screen currentScreen = Client.INSTANCE.screenManager.currentScreen;
        if (currentScreen != null) {
            JsonObject configForScreen = null;

            if (this.screenConfig.has(currentScreen.getName())) {
                JsonElement element = this.screenConfig.get(currentScreen.getName());
                if (element != null && element.isJsonObject()) {
                    configForScreen = element.getAsJsonObject();
                }
            }

            currentScreen.loadConfig(configForScreen != null ? configForScreen : new JsonObject());
        }
    }

    public void saveAlts() {
        JsonArray jsonArray = new JsonArray();

        for (Account account : Client.INSTANCE.accountManager.accounts) {
            jsonArray.add(new JsonParser().parse(account.toJson()).getAsJsonObject());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("alts", jsonArray);

        try {
            GsonUtils.save(jsonObject, new File(ConfigUtils.ALTS_FILE));
        } catch (IOException | JsonParseException e) {
            Client.LOGGER.error("Failed to save alts", e);
        }
    }

    public void loadAlts() {
        File altsFile = new File(ConfigUtils.ALTS_FILE);
        if (altsFile.exists()) {
            try (FileReader reader = new FileReader(altsFile)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                if (json.has("alts") && json.get("alts").isJsonArray()) {
                    JsonArray alts = json.getAsJsonArray("alts");
                    for (JsonElement altElement : alts) {
                        Account account = Account.fromJson(altElement.toString());
                        if (account != null) {
                            Client.INSTANCE.accountManager.accounts.add(account);
                        }
                    }
                }
            } catch (IOException | JsonParseException e) {
                Client.LOGGER.error("Failed to load alts", e);
            }
        }
    }

    public Profile getProfileByName(String name) {
        return this.profiles.stream()
                .filter(prof -> prof.name.equals(name))
                .findFirst()
                .orElseGet(() -> {
                    if (name.trim().equals("Default")) {
                        return new Profile(name, new JsonObject());
                    }
                    return null;
                });
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
