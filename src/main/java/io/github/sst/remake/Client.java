package io.github.sst.remake;

import io.github.sst.remake.bus.EventBus;
import io.github.sst.remake.manager.impl.ConfigManager;
import io.github.sst.remake.manager.impl.ScreenManager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.manager.impl.RPCManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements IMinecraft {
    // Constants
	public static final Logger LOGGER = LogManager.getLogger("Jello");
    public static final String VERSION = "1.0.0";
    public static final Client INSTANCE = new Client();
    public static final EventBus BUS = new EventBus();

    // Managers
    public final ScreenManager screenManager = new ScreenManager();
    public final ConfigManager configManager = new ConfigManager();
    public final RPCManager rpcManager = new RPCManager();

    public void start() {
        LOGGER.info("Initializing...");

        rpcManager.init();
        configManager.init();
        screenManager.init();

        LOGGER.info("Initialized.");
    }

    public void shutdown() {
        LOGGER.info("Shutting down...");

        rpcManager.shutdown();
        configManager.shutdown();
        screenManager.shutdown();

        LOGGER.info("Done.");
    }

}