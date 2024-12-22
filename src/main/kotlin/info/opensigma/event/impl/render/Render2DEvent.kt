package info.opensigma.event.impl.render

import net.minecraft.client.util.math.MatrixStack

data class Render2DEvent(
    val matrices: MatrixStack,
    val tickDelta: Float = 0.0f
)