package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.java.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OnlineProfilesManager {
    public final List<String> cachedOnlineProfiles = new ArrayList<>();

    public OnlineProfilesManager() {
    }

    public void cache(OnlineProfileListener listener) {
        new Thread(() -> {
            if (this.cachedOnlineProfiles.isEmpty())
                this.fetchOnlineProfiles();

            listener.onProfilesRetrieved(cachedOnlineProfiles);
        }).start();
    }

    public void fetchOnlineProfiles() {
        try {
            HttpGet request = new HttpGet("https://jelloconnect.sigmaclient.cloud/profiles.php?v=" + Client.VERSION + "remake");
            CloseableHttpResponse response = HttpClients.createDefault().execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        cachedOnlineProfiles.add(jsonArray.get(i).getAsString());
                    }
                }
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to fetch online profiles", e);
        }
    }

    public JsonObject fetchProfileConfig(String profileName) {
        try {
            HttpGet request = new HttpGet("https://jelloconnect.sigmaclient.cloud/profiles/" + StringUtils.encode(profileName) + ".profile?v=" + Client.VERSION + "remake");
            CloseableHttpResponse response = HttpClients.createDefault().execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    return JsonParser.parseString(content).getAsJsonObject();
                }
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to fetch online profile by the name {}", profileName, e);
        }
        return new JsonObject();
    }

    public Profile createProfileFromOnlineConfig(Profile base, String name) {
        Profile newProfile = new Profile(name, base);

        try {
            Profile settingsProfile = new Profile("settings", fetchProfileConfig(name).getAsJsonObject());
            Client.INSTANCE.moduleManager.loadJson(settingsProfile.content);
        } catch (JsonParseException e) {
            throw new RuntimeException("Failed to parse profile configuration", e);
        }

        return newProfile;
    }

    public interface OnlineProfileListener {
        void onProfilesRetrieved(List<String> profileNames);
    }
}
