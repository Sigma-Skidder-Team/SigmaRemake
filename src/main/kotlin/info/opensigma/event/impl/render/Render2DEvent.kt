package info.opensigma.event.impl.render

import net.minecraft.client.gui.DrawContext

data class Render2DEvent(
    val tickDelta: Float = 0.0f,
    val context: DrawContext
)