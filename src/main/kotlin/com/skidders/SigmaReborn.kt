package com.skidders;

import com.google.common.eventbus.EventBus
import com.skidders.sigma.managers.*
import com.skidders.sigma.processors.ScreenProcessor
import net.fabricmc.api.ModInitializer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class SigmaReborn : ModInitializer {
	companion object {
		const val MOD_ID = "sigma-reborn"

		val LOGGER: Logger = LogManager.getLogger(MOD_ID)
		val INSTANCE: SigmaReborn = SigmaReborn()
		val EVENT_BUS = EventBus(MOD_ID)
	}
	lateinit var moduleManager: ModuleManager;
	lateinit var screenProcessor: ScreenProcessor;

	override fun onInitialize() {
		LOGGER.info("Initializing Sigma Reborn");

	}
}