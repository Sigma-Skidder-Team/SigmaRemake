package com.skidders;

import com.skidders.sigma.handler.impl.ModuleHandler;
import com.skidders.sigma.handler.impl.ScreenHandler;
import com.skidders.sigma.util.client.interfaces.ISubscriber;
import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaReborn implements ModInitializer, ISubscriber {
	public static final String MOD_ID = "sigma-reborn";
	public static final Mode MODE = Mode.NONE;


	//making everything final for performance $$
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final SigmaReborn INSTANCE = new SigmaReborn();

	public final ModuleHandler moduleManager = new ModuleHandler();
	public final ScreenHandler screenHandler = new ScreenHandler();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Sigma Reborn");
	}

	public void onFinish() {
		moduleManager.init();
		screenHandler.init();
	}

	public enum Mode {
		NONE,
		CLASSIC,
		JELLO,
		NOADDONS
	}
}