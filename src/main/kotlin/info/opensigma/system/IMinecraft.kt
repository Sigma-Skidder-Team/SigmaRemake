package info.opensigma.system

import net.minecraft.client.MinecraftClient

interface IMinecraft {
    val client: MinecraftClient?
        get() = MinecraftClient.getInstance()
}
