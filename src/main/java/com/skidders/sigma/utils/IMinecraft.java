package com.skidders.sigma.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface IMinecraft {

    MinecraftClient mc = MinecraftClient.getInstance();
    Window window = mc.getWindow();

}
