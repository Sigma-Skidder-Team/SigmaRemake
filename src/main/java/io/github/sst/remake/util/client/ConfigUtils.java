package io.github.sst.remake.util.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtils {

    public static final File CLIENT_FOLDER = new File("jello-remake");

    public static final File PROFILES_FOLDER = new File(CLIENT_FOLDER, "profiles");
    public static final File WAYPOINTS_FOLDER = new File(CLIENT_FOLDER, "waypoints");
    public static final String EXTENSION = ".json";
    public static final String WAYPOINT_EXTENSION = ".jmap";

    public static final String ALTS_FILE = CLIENT_FOLDER + "/alts" + EXTENSION;
    public static final File CONFIG_FILE = new File(CLIENT_FOLDER, "config" + EXTENSION);
    public static final File SCREENS_FILE = new File(CLIENT_FOLDER, "screens" + EXTENSION);
    public static final File WAYPOINTS_FILE = new File(CLIENT_FOLDER, "waypoints" + EXTENSION);

    public static List<Profile> listAllProfiles() {
        List<Profile> profiles = new ArrayList<>();

        File[] files = PROFILES_FOLDER.listFiles();

        if (files == null) {
            Client.LOGGER.error("Failed to read profiles directory");
            return profiles;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(EXTENSION)) {
                String name = file.getName().substring(0, file.getName().length() - EXTENSION.length());
                JsonObject content = JsonParser
                        .parseString(FileUtils.readFile(file))
                        .getAsJsonObject();

                profiles.add(new Profile(name, content));
            }
        }

        return profiles;
    }

    public static boolean deleteProfile(Profile profile) {
        if (profile == null) {
            return false;
        }

        File file = new File(PROFILES_FOLDER, profile.name + EXTENSION);

        if (!file.exists() || !file.isFile()) {
            Client.LOGGER.error("Profile file not found: {}", file.getAbsolutePath());
            return false;
        }

        boolean deleted = file.delete();

        if (!deleted) {
            Client.LOGGER.error("Failed to delete profile: {}", profile.name);
        }

        return deleted;
    }

    public static boolean renameProfile(Profile profile, String newName) {
        if (profile == null || newName == null) {
            return false;
        }

        File oldFile = new File(PROFILES_FOLDER, profile.name + EXTENSION);
        File newFile = new File(PROFILES_FOLDER, newName + EXTENSION);

        if (!oldFile.exists() || !oldFile.isFile()) {
            Client.LOGGER.error("Profile file not found: {}", oldFile.getAbsolutePath());
            return false;
        }

        if (newFile.exists()) {
            Client.LOGGER.error("Profile with name already exists: {}", newName);
            return false;
        }

        boolean renamed = oldFile.renameTo(newFile);

        if (!renamed) {
            Client.LOGGER.error("Failed to rename profile from '{}' to '{}'", profile.name, newName);
        } else {
            profile.name = newName;
        }

        return renamed;
    }

}
