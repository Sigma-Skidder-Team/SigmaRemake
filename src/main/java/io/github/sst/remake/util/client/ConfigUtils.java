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
    public static final String EXTENSION = ".json";

    public static final String ALTS_FILE = CLIENT_FOLDER + "/alts" + EXTENSION;
    public static final String CONFIG_FILE = CLIENT_FOLDER + "/config" + EXTENSION;

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



}
