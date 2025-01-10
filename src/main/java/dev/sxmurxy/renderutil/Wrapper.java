package dev.sxmurxy.renderutil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface Wrapper {
		
	MinecraftClient MC = MinecraftClient.getInstance();
	Window WINDOW = MC.getWindow();
	
}
