package com.skidders.sigma.screens.pages

import com.skidders.sigma.utils.file.ResourceLoader
import com.skidders.sigma.utils.misc.TimeUtil
import com.skidders.sigma.utils.render.ColorUtil
import com.skidders.sigma.utils.render.RenderUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Overlay
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ResourceReloadMonitor
import org.lwjgl.opengl.GL11
import org.newdawn.slick.opengl.Texture

import java.util.Optional
import java.util.function.Consumer
import kotlin.math.min

class LoadingPage : Overlay {

    private val client: MinecraftClient
    private val reloadMonitor: ResourceReloadMonitor
    private val exceptionHandler: Consumer<Optional<Throwable>>
    private var reloading: Boolean

    private val logo: Texture
    private val blurredBackground: Texture

    private var progress: Float = 0F
    private var applyCompleteTime = -1L
    private var prepareCompleteTime = -1L

    constructor(client: MinecraftClient, monitor: ResourceReloadMonitor, exceptionHandler: Consumer<Optional<Throwable>>, reloading: Boolean) {
        this.client = client
        this.reloadMonitor = monitor
        this.exceptionHandler = exceptionHandler
        this.reloading = reloading

        logo = ResourceLoader.loadTexture("jello/loading/logo.png")
        blurredBackground = ResourceLoader.generateTexture("jello/loading/back.png", 0.25F, 25)
    }

    @Override
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val currentTime = TimeUtil.milliTime()
        if (this.reloading && (this.reloadMonitor.isPrepareStageComplete || this.client.currentScreen != null) && this.prepareCompleteTime == -1L) {
            this.prepareCompleteTime = currentTime
        }

        val applyCompleteProgress = if (applyCompleteTime > -1L) (currentTime - applyCompleteTime) / 200.0F else -1.0F
        val prepareCompleteProgress = if (prepareCompleteTime > -1L) (currentTime - prepareCompleteTime) / 100.0F else -1.0F
        val scaleFactor = 1.0F
        val estimatedSpeed = this.reloadMonitor.progress
        this.progress = this.progress * 0.95F + estimatedSpeed * 0.050000012F

        matrices.push()
        var framebufferRatio = 1111.0F
        if (this.client.window.width != 0) {
            framebufferRatio = (this.client.window.framebufferWidth / this.client.window.width).toFloat()
        }

        val guiScale = this.client.window.calculateScaleFactor(this.client.options.guiScale, this.client.options.forceUnicodeFont) * framebufferRatio
        GL11.glScalef(1.0F / guiScale, 1.0F / guiScale, 0.0F)
        renderLoadingScreen(matrices, scaleFactor, this.progress)
        matrices.pop()

        if (applyCompleteProgress >= 2.0F) {
            this.client.setOverlay(null)
        }

        if (this.applyCompleteTime == -1L && this.reloadMonitor.isApplyStageComplete && (!this.reloading || prepareCompleteProgress >= 2.0F)) {
            try {
                this.reloadMonitor.throwExceptions()
                this.exceptionHandler.accept(Optional.empty())
            } catch (throwable: Throwable) {
                this.exceptionHandler.accept(Optional.of(throwable))
            }

            this.applyCompleteTime = TimeUtil.milliTime()
            if (this.client.currentScreen != null) {
                this.client.currentScreen!!.init(this.client, this.client.window.scaledWidth, this.client.window.scaledHeight)
            }
        }
    }

    fun renderLoadingScreen(matrices: MatrixStack, opacity: Float, progress: Float) {
        matrices.push()
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        RenderUtil.drawImage(0.0F, 0.0F, MinecraftClient.getInstance().window.width.toFloat(), MinecraftClient.getInstance().window.height.toFloat(), blurredBackground, opacity)
        RenderUtil.drawRoundedRect2(0.0F, 0.0F, MinecraftClient.getInstance().window.width.toFloat(), MinecraftClient.getInstance().window.height.toFloat(), ColorUtil.applyAlpha(0, 0.75F))

        val logoWidth = 455
        val logoHeight = 78
        val logoX = (MinecraftClient.getInstance().window.width - logoWidth) / 2
        val logoY = (MinecraftClient.getInstance().window.height - logoHeight) / 2 - 14.0F * opacity

        RenderUtil.drawImage(logoX.toFloat(), logoY.toFloat(), logoWidth.toFloat(), logoHeight.toFloat(), logo, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.color, opacity))

        val clampedProgress = min(1.0F, progress * 1.02F)
        val progressBarOffset = 80

        if (opacity == 1.0F) {
            RenderUtil.drawRoundedRect(
                logoX.toFloat(), logoY + logoHeight + progressBarOffset, logoWidth.toFloat(), 20.0F, 10.0F, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.color, 0.3F * opacity)
            )
            RenderUtil.drawRoundedRect(
                (logoX + 1).toFloat(), (logoY + logoHeight + progressBarOffset + 1).toFloat(), (logoWidth - 2).toFloat(), 18.0F, 9.0F, ColorUtil.applyAlpha(ColorUtil.ClientColors.DEEP_TEAL.color, 1.0F * opacity)
            )
        }

        RenderUtil.drawRoundedRect(
            (logoX + 2).toFloat(), (logoY + logoHeight + progressBarOffset + 2).toFloat(), ((logoWidth - 4) * clampedProgress).toInt().toFloat(), 16.0F, 8.0F, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.color, 0.9F * opacity)
        )
        matrices.pop()
    }
}
