package io.github.sst.remake;

import io.github.sst.remake.bus.EventBus;
import io.github.sst.remake.manager.impl.*;
import io.github.sst.remake.util.IMinecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements IMinecraft {
    // Constants
	public static final Logger LOGGER = LogManager.getLogger("Sigma");
    public static final String VERSION = "1.0.0";
    public static final Client INSTANCE = new Client();
    public static final EventBus BUS = new EventBus();

    // Managers
    public final ScreenManager screenManager = new ScreenManager();
    public final TextureManager textureManager = new TextureManager();
    public final HUDManager hudManager = new HUDManager();
    public final ConfigManager configManager = new ConfigManager();
    public final RPCManager rpcManager = new RPCManager();

    public boolean loaded = false;

    public void start() {
        LOGGER.info("Initializing...");

        rpcManager.init();
        configManager.init();
        textureManager.init();
        screenManager.init();
        hudManager.init();

        LOGGER.info("Initialized.");
    }

    public void shutdown() {
        LOGGER.info("Shutting down...");

        rpcManager.shutdown();
        configManager.shutdown();
        screenManager.shutdown();
        hudManager.shutdown();
        textureManager.shutdown();

        LOGGER.info("Done.");
    }

}