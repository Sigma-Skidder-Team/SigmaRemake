package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.profile.Profile;
import io.github.sst.remake.util.http.NetUtils;
import io.github.sst.remake.util.java.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OnlineProfileManager {
    public final List<String> cachedProfileNames = new ArrayList<>();

    public void getOnlineProfileNames(ProfileNamesListener listener) {
        new Thread(() -> {
            if (this.cachedProfileNames.isEmpty())
                this.fetchOnlineProfileNames();

            listener.onProfileNamesReceived(cachedProfileNames);
        }).start();
    }

    public void fetchOnlineProfileNames() {
        try {
            HttpGet request = new HttpGet("https://jelloconnect.sigmaclient.cloud/profiles.php?v=" + Client.VERSION + "remake");
            CloseableHttpResponse response = NetUtils.getHttpClient().execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    JsonArray jsonArray = new JsonParser().parse(content).getAsJsonArray();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        cachedProfileNames.add(jsonArray.get(i).getAsString());
                    }
                }
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to fetch online profiles", e);
        }
    }

    public JsonObject fetchOnlineProfileConfig(String profileName) {
        try {
            HttpGet request = new HttpGet("https://jelloconnect.sigmaclient.cloud/profiles/" + StringUtils.encode(profileName) + ".profile?v=" + Client.VERSION + "remake");
            CloseableHttpResponse response = NetUtils.getHttpClient().execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    return new JsonParser().parse(content).getAsJsonObject();
                }
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to fetch online profile by the name {}", profileName, e);
        }
        return new JsonObject();
    }

    public Profile downloadOnlineProfile(String name) {
        try {
            JsonObject config = fetchOnlineProfileConfig(name);
            if (config.size() != 0) {
                return new Profile(name, config);
            }
        } catch (JsonParseException e) {
            Client.LOGGER.error("Failed to parse profile configuration for {}", name, e);
        }
        return null;
    }

    public interface ProfileNamesListener {
        void onProfileNamesReceived(List<String> profileNames);
    }
}
