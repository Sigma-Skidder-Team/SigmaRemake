package info.opensigma.system

import net.minecraft.client.Minecraft

interface IMinecraft {
    val client: Minecraft?
        get() = Minecraft.getInstance()
}
