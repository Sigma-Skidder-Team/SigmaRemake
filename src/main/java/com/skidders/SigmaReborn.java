package com.skidders;

import com.google.common.eventbus.EventBus;
import com.skidders.sigma.managers.FontManager;
import com.skidders.sigma.managers.ModuleManager;
import com.skidders.sigma.processors.ScreenProcessor;
import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaReborn implements ModInitializer {
	public static final String MOD_ID = "sigma-reborn";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static SigmaReborn INSTANCE = new SigmaReborn();
	public static EventBus EVENT_BUS = new EventBus(MOD_ID);

	public ModuleManager moduleManager;
	public FontManager fontManager;
	public ScreenProcessor screenProcessor;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Sigma Reborn");

	}
}