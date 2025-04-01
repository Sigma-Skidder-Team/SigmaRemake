package com.skidders.sigma.screen.pages;

import com.skidders.sigma.util.system.file.ResourceLoader;
import com.skidders.sigma.util.system.TimeUtil;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReloadMonitor;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import java.util.Optional;
import java.util.function.Consumer;

public class LoadingPage extends Overlay {

    private final MinecraftClient client;
    private final ResourceReloadMonitor reloadMonitor;
    private final Consumer<Optional<Throwable>> exceptionHandler;
    private final boolean reloading;

    private static Texture logo, blurredBackground;

    private float progress;
    private long applyCompleteTime = -1L;
    private long prepareCompleteTime = -1L;

    public LoadingPage(MinecraftClient client, ResourceReloadMonitor monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading) {
        this.client = client;
        this.reloadMonitor = monitor;
        this.exceptionHandler = exceptionHandler;
        this.reloading = reloading;

        logo = ResourceLoader.loadTexture("loading/logo.png");
        blurredBackground = ResourceLoader.generateTexture("loading/back.png", 0.25F, 25);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long currentTime = TimeUtil.milliTime();
        if (this.reloading && (this.reloadMonitor.isPrepareStageComplete() || this.client.currentScreen != null) && this.prepareCompleteTime == -1L) {
            this.prepareCompleteTime = currentTime;
        }

        float applyCompleteProgress = this.applyCompleteTime > -1L ? (float) (currentTime - this.applyCompleteTime) / 200.0F : -1.0F;
        float prepareCompleteProgress = this.prepareCompleteTime > -1L ? (float) (currentTime - this.prepareCompleteTime) / 100.0F : -1.0F;
        float scaleFactor = 1.0F;
        float estimatedSpeed = this.reloadMonitor.getProgress();
        this.progress = this.progress * 0.95F + estimatedSpeed * 0.050000012F;

        matrices.push();
        float framebufferRatio = 1111.0F;
        if (this.client.getWindow().getWidth() != 0) {
            framebufferRatio = (float) (this.client.getWindow().getFramebufferWidth() / this.client.getWindow().getWidth());
        }

        float guiScale = (float) this.client.getWindow().calculateScaleFactor(this.client.options.guiScale, this.client.options.forceUnicodeFont) * framebufferRatio;
        GL11.glScalef(1.0F / guiScale, 1.0F / guiScale, 0.0F);
        renderLoadingScreen(matrices, scaleFactor, this.progress);
        matrices.pop();

        if (applyCompleteProgress >= 2.0F) {
            this.client.setOverlay(null);
        }

        if (this.applyCompleteTime == -1L && this.reloadMonitor.isApplyStageComplete() && (!this.reloading || prepareCompleteProgress >= 2.0F)) {
            try {
                this.reloadMonitor.throwExceptions();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.exceptionHandler.accept(Optional.of(throwable));
            }

            this.applyCompleteTime = TimeUtil.milliTime();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
            }
        }
    }

    public static void renderLoadingScreen(MatrixStack matrices, float opacity, float progress) {
        matrices.push();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        RenderUtil.drawImage(0.0F, 0.0F, (float) MinecraftClient.getInstance().getWindow().getWidth(), (float) MinecraftClient.getInstance().getWindow().getHeight(), blurredBackground, opacity);
        RenderUtil.drawRoundedRect2(0.0F, 0.0F, (float) MinecraftClient.getInstance().getWindow().getWidth(), (float) MinecraftClient.getInstance().getWindow().getHeight(), ColorUtil.applyAlpha(0, 0.75F));

        int logoWidth = 455;
        int logoHeight = 78;
        float logoX = (float) (MinecraftClient.getInstance().getWindow().getWidth() - logoWidth) / 2;
        float logoY = (float) (MinecraftClient.getInstance().getWindow().getHeight() - logoHeight) / 2 - 14.0F * opacity;

        RenderUtil.drawImage((float) logoX, (float) logoY, (float) logoWidth, (float) logoHeight, logo, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), opacity));

        float clampedProgress = Math.min(1.0F, progress * 1.02F);
        float progressBarOffset = 80;

        if (opacity == 1.0F) {
            RenderUtil.drawRoundedRect(
                    logoX, logoY + logoHeight + progressBarOffset, (float) logoWidth, 20.0F, 10.0F, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F * opacity)
            );
            RenderUtil.drawRoundedRect(
                    logoX + 1,
                    logoY + logoHeight + progressBarOffset + 1,
                    (float) (logoWidth - 2),
                    18.0F,
                    9.0F,
                    ColorUtil.applyAlpha(ColorUtil.ClientColors.DEEP_TEAL.getColor(), 1.0F * opacity)
            );
        }

        RenderUtil.drawRoundedRect(
                logoX + 2,
                logoY + logoHeight + progressBarOffset + 2,
                (float) ((int) ((float) (logoWidth - 4) * clampedProgress)),
                16.0F,
                8.0F,
                ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F * opacity)
        );
        matrices.pop();
    }

}
