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
import org.lwjgl.opengl.GL30
import java.util.concurrent.ConcurrentLinkedQueue

object BloomShader {
    private val bloom: Shader = Shader("bloom.frag")
    private val renderQueue: ConcurrentLinkedQueue<IRenderCall> = Queues.newConcurrentLinkedQueue()
    private val inFrameBuffer: Framebuffer =
        Framebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC)
    private val outFrameBuffer: Framebuffer =
        Framebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC)

    fun registerRenderCall(rc: IRenderCall?) {
        renderQueue.add(rc)
    }

    fun draw(radius: Int) {
        if (renderQueue.isEmpty()) return

        setupBuffer(inFrameBuffer)
        setupBuffer(outFrameBuffer)

        inFrameBuffer.beginWrite(true)

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute()
        }

        outFrameBuffer.beginWrite(true)

        bloom.load()
        bloom.setUniformf("radius", radius.toFloat())
        bloom.setUniformi("sampler1", 0)
        bloom.setUniformi("sampler2", 20)
        bloom.setUniformfb("kernel", StencilUtils.getKernel(radius))
        bloom.setUniformf("texelSize", 1.0f / window.getWidth() as Float, 1.0f / window.getHeight() as Float)
        bloom.setUniformf("direction", 2.0f, 0.0f)

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA)
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f)

        inFrameBuffer.beginRead()
        Shader.draw()

        mc.getFramebuffer().beginWrite(false)
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)

        bloom.setUniformf("direction", 0.0f, 2.0f)

        outFrameBuffer.beginRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE20)
        inFrameBuffer.beginRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        Shader.draw()

        bloom.unload()
        inFrameBuffer.endRead()
        GlStateManager.disableBlend()
    }

    private fun setupBuffer(frameBuffer: Framebuffer): Framebuffer {
        if (frameBuffer.textureWidth !== window.getWidth() || frameBuffer.textureHeight !== window.getHeight()) frameBuffer.resize(
            window.getWidth(),
            window.getHeight(),
            MinecraftClient.IS_SYSTEM_MAC
        )
        else frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        frameBuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        return frameBuffer
    }
}