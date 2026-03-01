package io.github.sst.remake.gui.screen.loading;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Consumer;

public class LoadingScreen extends Overlay implements IMinecraft {
    private float smoothedProgress;
    private long applyCompleteTimeMs = -1L;
    private long prepareCompleteTimeMs = -1L;

    private final ResourceReload reloadMonitor;
    private final Consumer<Optional<Throwable>> exceptionHandler;
    private final boolean isReloading;

    public LoadingScreen(ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading) {
        this.reloadMonitor = monitor;
        this.exceptionHandler = exceptionHandler;
        this.isReloading = reloading;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long nowMs = Util.getMeasuringTimeMs();

        if (this.isReloading
                && (this.reloadMonitor.isPrepareStageComplete() || client.currentScreen != null)
                && this.prepareCompleteTimeMs == -1L) {
            this.prepareCompleteTimeMs = nowMs;
        }

        float secondsSinceApplyComplete = this.applyCompleteTimeMs > -1L
                ? (float) (nowMs - this.applyCompleteTimeMs) / 200.0F
                : -1.0F;

        float secondsSincePrepareComplete = this.prepareCompleteTimeMs > -1L
                ? (float) (nowMs - this.prepareCompleteTimeMs) / 100.0F
                : -1.0F;

        float backgroundOpacity = 1.0F;

        float rawProgress = this.reloadMonitor.getProgress();
        this.smoothedProgress = this.smoothedProgress * 0.95F + rawProgress * 0.050000012F;

        GL11.glPushMatrix();

        float framebufferToWindowScale = 1111.0F;
        if (client.getWindow().getWidth() != 0) {
            framebufferToWindowScale = (float) (client.getWindow().getFramebufferWidth() / client.getWindow().getWidth());
        }

        float guiScale = (float) client.getWindow().calculateScaleFactor(client.options.guiScale, client.forcesUnicodeFont())
                * framebufferToWindowScale;

        GL11.glScalef(1.0F / guiScale, 1.0F / guiScale, 0.0F);

        RenderUtils.renderFadeOut(backgroundOpacity, this.smoothedProgress);

        GL11.glPopMatrix();

        if (secondsSinceApplyComplete >= 2.0F) {
            client.setOverlay(null);
        }

        if (this.applyCompleteTimeMs == -1L
                && this.reloadMonitor.isComplete()
                && (!this.isReloading || secondsSincePrepareComplete >= 2.0F)) {
            try {
                this.reloadMonitor.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable t) {
                this.exceptionHandler.accept(Optional.of(t));
            }

            this.applyCompleteTimeMs = Util.getMeasuringTimeMs();

            if (client.currentScreen != null) {
                client.currentScreen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            }
        }
    }
}