package com.skidders.sigma.utils.render.image

import com.mojang.blaze3d.platform.GlStateManager
import com.skidders.sigma.utils.file.FileUtil
import com.skidders.sigma.utils.mc
import kotlinx.io.IOException
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.util.Identifier
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import java.awt.image.BufferedImage
import java.nio.BufferOverflowException
import java.nio.ReadOnlyBufferException

object TextureLoader {
    fun loadTexture1(image: BufferedImage): Int {
        val pixels: IntArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val buffer: java.nio.ByteBuffer = BufferUtils.createByteBuffer(pixels.size * 4)

        try {
            for (pixel: Int in pixels) {
                buffer.put(((pixel shr 16) and 0xFF).toByte())
                buffer.put(((pixel shr 8) and 0xFF).toByte())
                buffer.put((pixel and 0xFF).toByte())
                buffer.put(((pixel shr 24) and 0xFF).toByte())
            }
            buffer.flip()
        } catch (ex: BufferOverflowException) {
            return -1
        } catch (ex: ReadOnlyBufferException) {
            return -1
        }

        val textureID: Int = GlStateManager.genTextures()
        GlStateManager.bindTexture(textureID)
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR)
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR)
        GL30.glTexImage2D(
            GL30.GL_TEXTURE_2D,
            0,
            GL30.GL_RGBA8,
            image.width,
            image.height,
            0,
            GL30.GL_RGBA,
            GL30.GL_UNSIGNED_BYTE,
            buffer
        )
        GlStateManager.bindTexture(0)

        return textureID
    }

    fun loadTexture2(image: BufferedImage): Int {
        val pixels: IntArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val buffer: java.nio.ByteBuffer = BufferUtils.createByteBuffer(pixels.size * 4)

        for (pixel: Int in pixels) {
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put((pixel and 0xFF).toByte())
            buffer.put(((pixel shr 24) and 0xFF).toByte())
        }
        buffer.flip()

        val textureID: Int = GlStateManager.genTextures()
        GlStateManager.bindTexture(textureID)
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA8,
            image.width,
            image.height,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            buffer
        )
        GlStateManager.bindTexture(0)
        return textureID
    }

    fun getTextureId(identifier: Identifier?): Int {
        val abstractTexture: AbstractTexture? = mc.textureManager.getTexture(identifier)
        if (abstractTexture != null) {
            return abstractTexture.glId
        }
        return 0
    }

    class ImageParser(val width: Int, val height: Int, image: java.nio.ByteBuffer?) {
        val image: java.nio.ByteBuffer?

        init {
            this.image = image
        }

        companion object {
            @JvmStatic
            fun loadImage(resource: String, path: String): ImageParser {
                try {
                    FileUtil.copyResourceToFile(resource, path)
                } catch (e: IOException) {
                    throw java.lang.RuntimeException(e)
                }

                var image: java.nio.ByteBuffer?
                var width: Int
                var height: Int

                MemoryStack.stackPush().use { stack ->
                    val comp: java.nio.IntBuffer = stack.mallocInt(1)
                    val w: java.nio.IntBuffer = stack.mallocInt(1)
                    val h: java.nio.IntBuffer = stack.mallocInt(1)

                    image = stbi_load(path, w, h, comp, 4)
                    if (image == null) {
                        throw java.lang.RuntimeException("Could not load image $path")
                    }
                    width = w.get()
                    height = h.get()
                }
                return ImageParser(width, height, image)
            }
        }
    }
}