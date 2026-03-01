package io.github.sst.remake.util.http.token;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import net.minecraft.client.util.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenLoginUtils {
    private static final Gson gson = new Gson();

    public static Session setSession(String accessToken) {
        try {
            JsonObject profileData = fetchProfile(accessToken);
            if (profileData == null) {
                return null;
            }

            String uuidString = profileData.get("id").getAsString();
            String username = profileData.get("name").getAsString();

            String formattedUuid;
            if (uuidString.contains("-")) {
                formattedUuid = uuidString;
            } else {
                formattedUuid = uuidString.substring(0, 8) + "-" +
                        uuidString.substring(8, 12) + "-" +
                        uuidString.substring(12, 16) + "-" +
                        uuidString.substring(16, 20) + "-" +
                        uuidString.substring(20);
            }

            return new Session(
                    username,
                    formattedUuid,
                    accessToken,
                    "legacy"
            );
        } catch (Exception e) {
            Client.LOGGER.error("Failed to verify token", e);
            return null;
        }
    }

    private static JsonObject fetchProfile(String accessToken) {
        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoInput(true);

            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBody = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBody.append(inputLine);
                }
                in.close();

                return gson.fromJson(responseBody.toString(), JsonObject.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            Client.LOGGER.warn("Failed to fetch profile", e);
            return null;
        }
    }
}