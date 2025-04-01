package com.skidders.sigma.utils.render.shader.shader

import com.mojang.blaze3d.platform.GlStateManager
import com.skidders.SigmaReborn
import com.skidders.sigma.utils.window
import kotlinx.io.IOException
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL30
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer
import java.util.stream.Collectors

class Shader(fragmentShaderName: String) {
    private val programId: Int

    init {
        val programId: Int = GlStateManager.createProgram()
        try {
            val fragmentShader: Int = GlStateManager.createShader(GL30.GL_FRAGMENT_SHADER)
            GlStateManager.shaderSource(fragmentShader, getShaderSource(fragmentShaderName))
            GlStateManager.compileShader(fragmentShader)

            val isFragmentCompiled: Int = GL30.glGetShaderi(fragmentShader, GL30.GL_COMPILE_STATUS)
            if (isFragmentCompiled == 0) {
                GlStateManager.deleteShader(fragmentShader)
                java.lang.System.err.println("Fragment shader couldn't compile. It has been deleted.")
            }

            GlStateManager.attachShader(programId, VERTEX_SHADER)
            GlStateManager.attachShader(programId, fragmentShader)
            GlStateManager.linkProgram(programId)
        } catch (e: java.lang.Exception) {
            SigmaReborn.LOGGER.error(e.message)
        }
        this.programId = programId
    }

    fun load() {
        GlStateManager.useProgram(programId)
    }

    fun unload() {
        GlStateManager.useProgram(0)
    }

    fun getUniform(name: String?): Int {
        return GL30.glGetUniformLocation(programId, name)
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc: Int = GL30.glGetUniformLocation(programId, name)
        when (args.size) {
            1 -> GL30.glUniform1f(loc, args.get(0))
            2 -> GL30.glUniform2f(loc, args.get(0), args.get(1))
            3 -> GL30.glUniform3f(loc, args.get(0), args.get(1), args.get(2))
            4 -> GL30.glUniform4f(loc, args.get(0), args.get(1), args.get(2), args.get(3))
        }
    }

    fun setUniformi(name: String?, vararg args: Int) {
        val loc: Int = GL30.glGetUniformLocation(programId, name)
        when (args.size) {
            1 -> GL30.glUniform1i(loc, args.get(0))
            2 -> GL30.glUniform2i(loc, args.get(0), args.get(1))
            3 -> GL30.glUniform3i(loc, args.get(0), args.get(1), args.get(2))
            4 -> GL30.glUniform4i(loc, args.get(0), args.get(1), args.get(2), args.get(3))
        }
    }

    fun setUniformfb(name: String?, buffer: FloatBuffer?) {
        GL30.glUniform1fv(GL30.glGetUniformLocation(programId, name), buffer)
    }

    companion object {
        val VERTEX_SHADER: Int

        init {
            VERTEX_SHADER = GlStateManager.createShader(GL30.GL_VERTEX_SHADER)
            GlStateManager.shaderSource(VERTEX_SHADER, getShaderSource("vertex.vert"))
            GlStateManager.compileShader(VERTEX_SHADER)
        }

        @kotlin.jvm.JvmOverloads
        fun draw(
            x: Double = 0.0,
            y: Double = 0.0,
            width: Double = window.getScaledWidth().toDouble(),
            height: Double = window.getScaledHeight().toDouble()
        ) {
            GL30.glBegin(GL_QUADS)
            GL30.glTexCoord2d(0.0, 0.0)
            GL30.glVertex2d(x, y)
            GL30.glTexCoord2d(0.0, 1.0)
            GL30.glVertex2d(x, y + height)
            GL30.glTexCoord2d(1.0, 1.0)
            GL30.glVertex2d(x + width, y + height)
            GL30.glTexCoord2d(1.0, 0.0)
            GL30.glVertex2d(x + width, y)
            GL30.glEnd()
        }

        fun getShaderSource(fileName: String): String {
            val source: String
            val bufferedReader: BufferedReader = BufferedReader(
                InputStreamReader(
                    SigmaReborn::class.java
                        .getResourceAsStream("/assets/" + SigmaReborn.MOD_ID + "/jello/shaders/" + fileName)
                )
            )
            source = bufferedReader.lines().filter { str: String -> str.isNotEmpty() }.map { str: String ->
                return@map str.replace("\t", "")
            }.collect(Collectors.joining("\n"))
            try {
                bufferedReader.close()
            } catch (ignored: IOException) {
            }
            return source
        }
    }
}