package com.skidders.sigma.utils.render.shader.shader.impl

import com.google.common.collect.Queues
import com.mojang.blaze3d.platform.GlStateManager
import com.skidders.sigma.utils.mc
import com.skidders.sigma.utils.render.interfaces.IRenderCall
import com.skidders.sigma.utils.render.shader.StencilUtils
import com.skidders.sigma.utils.render.shader.shader.Shader
import com.skidders.sigma.utils.window
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import java.util.concurrent.ConcurrentLinkedQueue

object BlurShader {
    private val blur: Shader = Shader("blur.frag")
    private val renderQueue: ConcurrentLinkedQueue<IRenderCall> = Queues.newConcurrentLinkedQueue()
    private val inFrameBuffer: Framebuffer = Framebuffer(
        (window.width / 2.0).toInt(),
        (window.height / 2.0).toInt(),
        true,
        MinecraftClient.IS_SYSTEM_MAC
    )
    private val outFrameBuffer: Framebuffer = Framebuffer(
        (window.width / 2.0).toInt(),
        (window.height / 2.0).toInt(),
        true,
        MinecraftClient.IS_SYSTEM_MAC
    )

    fun registerRenderCall(rc: IRenderCall?) {
        renderQueue.add(rc)
    }

    //i forgor how to use it wait
    fun draw(radius: Int) {
        if (renderQueue.isEmpty()) return

        setupBuffer(inFrameBuffer)
        setupBuffer(outFrameBuffer)

        inFrameBuffer.beginWrite(true)

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute()
        }

        outFrameBuffer.beginWrite(true)

        blur.load()
        blur.setUniformf("radius", radius.toFloat())
        blur.setUniformi("sampler1", 0)
        blur.setUniformi("sampler2", 20)
        blur.setUniformfb("kernel", StencilUtils.getKernel(radius))
        blur.setUniformf("texelSize", 1.0f / window.width.toFloat(), 1.0f / window.height.toFloat())
        blur.setUniformf("direction", 2.0f, 0.0f)

        GlStateManager.disableBlend()
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        mc.framebuffer.beginRead()
        Shader.draw()

        mc.framebuffer.beginWrite(true)

        blur.setUniformf("direction", 0.0f, 2.0f)

        outFrameBuffer.beginRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE20)
        inFrameBuffer.beginRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        Shader.draw()

        blur.unload()
        inFrameBuffer.endRead()
        GlStateManager.disableBlend()
    }

    private fun setupBuffer(frameBuffer: Framebuffer): Framebuffer {
        if (frameBuffer.textureWidth != (window.width / 2.0).toInt() || frameBuffer.textureHeight != (window.height / 2.0).toInt()) frameBuffer.resize(
            (window.width / 2.0).toInt(),
            (window.height / 2.0).toInt(),
            MinecraftClient.IS_SYSTEM_MAC
        )
        else frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC)

        return frameBuffer
    }
}