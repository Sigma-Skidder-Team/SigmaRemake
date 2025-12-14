package io.github.sst.remake.util.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class GsonUtils {
    public static boolean getBooleanOrDefault(JsonObject jsonObject, String key, boolean defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }

        JsonElement jsonElement;

        try {
            jsonElement = jsonObject.get(key);

            if (jsonElement == null) {
                return defaultValue;
            }

            return jsonElement.getAsBoolean();
        } catch (JsonParseException e) {
            return defaultValue;
        }
    }

    public static int getIntOrDefault(JsonObject jsonObject, String key, int defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }

        JsonElement jsonElement;

        try {
            jsonElement = jsonObject.get(key);

            if (jsonElement == null) {
                return defaultValue;
            }

            return jsonElement.getAsInt();
        } catch (JsonParseException e) {
            return defaultValue;
        }
    }

    public static float getFloatOrDefault(JsonObject jsonObject, String key, float defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }

        JsonElement jsonElement;

        try {
            jsonElement = jsonObject.get(key);

            if (jsonElement == null) {
                return defaultValue;
            }

            return (float) jsonElement.getAsDouble();
        } catch (JsonParseException e) {
            return defaultValue;
        }
    }

    public static String getStringOrDefault(JsonObject jsonObject, String key, String defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }

        JsonElement jsonElement;

        try {
            jsonElement = jsonObject.get(key);

            if (jsonElement == null) {
                return defaultValue;
            }

            return jsonElement.getAsString();
        } catch (JsonParseException e) {
            return defaultValue;
        }
    }

    public static JsonObject getJSONObjectOrNull(JsonObject jsonObject, String key) {
        if (jsonObject == null) {
            return null;
        }

        try {
            return jsonObject.getAsJsonObject(key);
        } catch (JsonParseException e) {
            return null;
        }
    }

    public static JsonArray getJSONArrayOrNull(JsonObject jsonObject, String key) {
        if (jsonObject == null) {
            return null;
        }

        try {
            return jsonObject.getAsJsonArray(key);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
