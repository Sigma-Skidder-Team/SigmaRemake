package com.skidders.sigma.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.client.util.Window
import net.minecraft.client.world.ClientWorld

val mc: MinecraftClient
    inline get() = MinecraftClient.getInstance()!!
val player: ClientPlayerEntity
    inline get() = mc.player!!
val world: ClientWorld
    inline get() = mc.world!!
val network: ClientPlayNetworkHandler
    inline get() = mc.networkHandler!!
val interaction: ClientPlayerInteractionManager
    inline get() = mc.interactionManager!!

val window: Window
    inline get() = mc.window