package com.skidders.sigma.util.client.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface IMinecraft {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window window = mc.getWindow();
}