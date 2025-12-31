package io.github.sst.remake.util.io;

import com.google.gson.*;
import io.github.sst.remake.Client;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean isFreshConfig = false;

    public static void save(JsonObject jsonObject, File file) throws IOException {
        String json = GSON.toJson(jsonObject);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.write(json, outputStream, StandardCharsets.UTF_8);
        }
    }

    public static JsonObject readJsonFile(File file) throws IOException {
        if (!file.exists()) {
            Client.LOGGER.info("Config does not exist. Creating new config file...");
            isFreshConfig = true;
            file.createNewFile();
            return new JsonObject();
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            if (content == null || content.trim().isEmpty()) {
                Client.LOGGER.warn("Config file is empty");
                return new JsonObject();
            }

            try {
                return JsonParser.parseString(content).getAsJsonObject();
            } catch (JsonParseException e) {
                Client.LOGGER.warn(
                        "Failed to parse config JSON. Preferences will not be loaded.",
                        e
                );
                return new JsonObject();
            }
        }
    }

}
