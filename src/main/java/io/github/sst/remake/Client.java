package io.github.sst.remake;

import com.google.gson.JsonObject;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.initalizer.RPCInitalizer;
import io.github.sst.remake.util.io.FileUtils;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Client implements IMinecraft {
	public static final String MOD_ID = "Jello";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String VERSION = "1.0.0";

    @Getter
    private static final Client instance = new Client();

    public final File file = new File("sigma5");

    public JsonObject config;

    public void start() {
        LOGGER.info("Initializing...");

        try {
            if (!file.exists()) {
                file.mkdirs();
            }

            config = FileUtils.readJsonFile(new File(file + "/config.json"));
        } catch (IOException e) {
            LOGGER.error("Failed to load default config", e);
        }

        RPCInitalizer.init();

        LOGGER.info("Initialized.");
    }

    public void shutdown() {
        LOGGER.info("Shutting down...");

        RPCInitalizer.shutdown();

        try {
            FileUtils.save(config, new File(file + "/config.json"));
        } catch (IOException exc) {
            LOGGER.error("Unable to shutdown correctly. Config may be corrupt?", exc);
        }

        LOGGER.info("Done.");
    }

}