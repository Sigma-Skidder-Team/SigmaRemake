package info.opensigma.event.impl.render

import com.mojang.blaze3d.vertex.PoseStack

data class Render2DEvent(
    val matrices: PoseStack,
    val tickDelta: Float = 0.0f
)