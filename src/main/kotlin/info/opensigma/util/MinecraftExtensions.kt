package info.opensigma.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text

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

val MinecraftClient.chatHud: ChatHud
    inline get() = mc.inGameHud.chatHud

fun ChatHud.addMessage(message: String) {
    addMessage(Text.of(message))
}