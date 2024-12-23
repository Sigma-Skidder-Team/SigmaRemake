package info.opensigma.module.impl.player

import com.google.common.eventbus.Subscribe
import info.opensigma.OpenSigma
import info.opensigma.module.Module
import info.opensigma.module.data.ModuleCategory
import info.opensigma.event.impl.TickEvent
import info.opensigma.util.addMessage
import info.opensigma.util.chatHud
import info.opensigma.util.mc
import info.opensigma.util.network
import info.opensigma.util.player
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import org.lwjgl.glfw.GLFW

class NoFall : Module(
    "NoFall",
    "Prevents you from taking fall damage",
    GLFW.GLFW_KEY_P,
    ModuleCategory.PLAYER) {
    override fun onEnable() {
        super.onEnable()
        mc.chatHud.addMessage("enabled")
        OpenSigma.instance.eventBus.subscribe(this)
    }

    override fun onDisable() {
        super.onDisable()
        mc.chatHud.addMessage("disabled")
        OpenSigma.instance.eventBus.unsubscribe(this)
    }
    @EventHandler
    @Suppress("unused")
    fun onTickEvent(event: TickEvent) {
        network.sendPacket(
            PlayerMoveC2SPacket.OnGroundOnly(
                true, player.horizontalCollision
            )
        )
    }
}