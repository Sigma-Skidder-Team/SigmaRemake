package com.skidders.sigma.utils.render.interfaces

import net.minecraft.util.Identifier

interface GameRendererAccessor {
    fun `sigmaRemake$invokeLoadShader`(identifier: Identifier?)
}