package info.opensigma.module.impl.player

import info.opensigma.OpenSigma
import info.opensigma.module.Module
import info.opensigma.module.data.ModuleCategory
import info.opensigma.event.impl.TickEvent
import info.opensigma.util.addMessage
import info.opensigma.util.chat
import info.opensigma.util.mc
import info.opensigma.util.network
import info.opensigma.util.player
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import org.lwjgl.glfw.GLFW

class NoFall : Module(
    "NoFall",
    "Prevents you from taking fall damage",
    GLFW.GLFW_KEY_P,
    ModuleCategory.PLAYER) {
    override fun onEnable() {
        super.onEnable()
        mc.chat.addMessage("enabled")
        OpenSigma.instance.eventBus.subscribe(this)
    }

    override fun onDisable() {
        super.onDisable()
        mc.chat.addMessage("disabled")
        OpenSigma.instance.eventBus.unsubscribe(this)
    }
    @EventHandler
    @Suppress("unused")
    fun onTickEvent(event: TickEvent) {
        if (player.fallDistance >= 0.1F && player.tickCount > 20) {
            player.fallDistance = 0F
            player.setOnGround(true)
            network.send(
                ServerboundMovePlayerPacket.StatusOnly(
                    true, player.horizontalCollision
                )
            )
        }
    }
}