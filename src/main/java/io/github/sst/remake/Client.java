package io.github.sst.remake;

import io.github.sst.remake.data.bus.EventBus;
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
    public final ModuleManager moduleManager = new ModuleManager();
    public final BindManager bindManager = new BindManager();
    public final AccountManager accountManager = new AccountManager();
    public final NotificationManager notificationManager = new NotificationManager();
    public final WaypointManager waypointManager = new WaypointManager();
    public final MusicManager musicManager = new MusicManager();
    public final RotationManager rotationManager = new RotationManager();

    public boolean loaded = false;

    public void start() {
        LOGGER.info("Starting Sigma Remake {}...", VERSION);

        rpcManager.init();
        textureManager.init();
        screenManager.init();
        hudManager.init();
        accountManager.init();
        moduleManager.init();
        rotationManager.init();
        bindManager.init();
        waypointManager.init();
        notificationManager.init();
        musicManager.init();
        configManager.init();

        LOGGER.info("Everything has been initialised.");
    }

    public void shutdown() {
        LOGGER.info("Shutting down...");

        rpcManager.shutdown();
        configManager.shutdown();
        accountManager.shutdown();
        bindManager.shutdown();
        rotationManager.shutdown();
        moduleManager.shutdown();
        textureManager.shutdown();
        hudManager.shutdown();
        screenManager.shutdown();
        notificationManager.shutdown();
        waypointManager.shutdown();
        musicManager.shutdown();

        LOGGER.info("Everything saved.");
    }

}