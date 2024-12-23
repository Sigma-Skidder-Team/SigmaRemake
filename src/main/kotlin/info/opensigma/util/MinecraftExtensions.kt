package info.opensigma.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component

val mc: Minecraft
    inline get() = Minecraft.getInstance()!!
val player: LocalPlayer
    inline get() = mc.player!!
val world: ClientLevel
    inline get() = mc.level!! // this is probably level
val network: ClientPacketListener
    inline get() = mc.connection!!
val interaction: MultiPlayerGameMode
    inline get() = mc.gameMode!!

val Minecraft.chat: ChatComponent
    inline get() = mc.gui.chat

fun ChatComponent.addMessage(message: String) {
    addMessage(Component.nullToEmpty(message))
}