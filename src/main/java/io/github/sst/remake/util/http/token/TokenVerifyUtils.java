package io.github.sst.remake.util.http.token;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenVerifyUtils {

    private static final int TIMEOUT = 10000;
    private static final Gson gson = new Gson();

    public static AuthResult authenticate(String token) {
        if (isBlank(token)) {
            return new AuthResult.Failure("Token is empty", true);
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);

            int statusCode = connection.getResponseCode();

            InputStream stream = (statusCode >= 200 && statusCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody = readStream(stream);

            switch (statusCode) {
                case 200: {
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    if (jsonResponse != null
                            && jsonResponse.has("id")
                            && jsonResponse.has("name")) {
                        return new AuthResult.Success();
                    } else {
                        return new AuthResult.Failure("Invalid response format", false);
                    }
                }
                case 401:
                    return new AuthResult.Failure("Invalid or expired token", true);
                case 404:
                    return new AuthResult.Failure("No Minecraft profile found", true);
                default:
                    return new AuthResult.Failure("Authentication failed: " + statusCode, false);
            }

        } catch (Exception e) {
            return new AuthResult.Failure("Connection error: " + e.getMessage(), false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static abstract class AuthResult {
        private AuthResult() {}

        public static final class Success extends AuthResult {
            public Success() {}
        }

        public static final class Failure extends AuthResult {
            private final String message;
            private final boolean tokenInvalid;

            public Failure(String message, boolean tokenInvalid) {
                this.message = message;
                this.tokenInvalid = tokenInvalid;
            }

            public String getMessage() {
                return message;
            }

            public boolean isTokenInvalid() {
                return tokenInvalid;
            }
        }
    }
}
