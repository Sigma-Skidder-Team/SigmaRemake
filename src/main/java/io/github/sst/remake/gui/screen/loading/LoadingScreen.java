package io.github.sst.remake.gui.screen.loading;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import org.newdawn.slick.opengl.texture.Texture;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Consumer;

public class LoadingScreen extends Overlay implements IMinecraft {

    public static Texture sigmaLogo;
    public static Texture back;
    public static Texture background;

    private float progress;
    private long applyCompleteTime = -1L;
    private long prepareCompleteTime = -1L;

    private final ResourceReload reloadMonitor;
    private final Consumer<Optional<Throwable>> exceptionHandler;
    private final boolean reloading;

    public LoadingScreen(ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading) {
        this.reloadMonitor = monitor;
        this.exceptionHandler = exceptionHandler;
        this.reloading = reloading;

        sigmaLogo = Resources.loadTexture("user/logo.png");
        back = Resources.loadTexture("loading/back.png");
        background = Resources.createPaddedBlurredTexture("loading/back.png", 0.25F, 25);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long var9 = Util.getMeasuringTimeMs();
        if (this.reloading && (this.reloadMonitor.isPrepareStageComplete() || client.currentScreen != null) && this.prepareCompleteTime == -1L) {
            this.prepareCompleteTime = var9;
        }

        float var11 = this.applyCompleteTime > -1L ? (float) (var9 - this.applyCompleteTime) / 200.0F : -1.0F;
        float var12 = this.prepareCompleteTime > -1L ? (float) (var9 - this.prepareCompleteTime) / 100.0F : -1.0F;
        float var13 = 1.0F;
        float var16 = this.reloadMonitor.getProgress();
        this.progress = this.progress * 0.95F + var16 * 0.050000012F;
        GL11.glPushMatrix();
        float var17 = 1111.0F;
        if (client.getWindow().getWidth() != 0) {
            var17 = (float) (client.getWindow().getFramebufferWidth() / client.getWindow().getWidth());
        }

        float var18 = (float) client.getWindow().calculateScaleFactor(client.options.guiScale, client.forcesUnicodeFont()) * var17;
        GL11.glScalef(1.0F / var18, 1.0F / var18, 0.0F);
        renderFadeOut(var13, this.progress);
        GL11.glPopMatrix();
        if (var11 >= 2.0F) {
            client.setOverlay(null);
        }

        if (this.applyCompleteTime == -1L && this.reloadMonitor.isComplete() && (!this.reloading || var12 >= 2.0F)) {
            try {
                this.reloadMonitor.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var20) {
                this.exceptionHandler.accept(Optional.of(var20));
            }

            this.applyCompleteTime = Util.getMeasuringTimeMs();
            if (client.currentScreen != null) {
                client.currentScreen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            }
        }
    }

    public static void renderFadeOut(float bgOpacity, float var1) {
        GL11.glEnable(3008);
        GL11.glEnable(3042);
        RenderUtils.drawImage(0.0F, 0.0F, (float) client.getWindow().getWidth(), (float) client.getWindow().getHeight(), background, bgOpacity);
        RenderUtils.drawRoundedRect2(0.0F, 0.0F, (float) client.getWindow().getWidth(), (float) client.getWindow().getHeight(), ColorHelper.applyAlpha(0, 0.75F));

        int var4 = 455;
        int var5 = 78;
        int var6 = (client.getWindow().getWidth() - var4) / 2;
        int var7 = Math.round((float) ((client.getWindow().getHeight() - var5) / 2) - 14.0F * bgOpacity);
        float var8 = 0.75F + bgOpacity * bgOpacity * bgOpacity * bgOpacity * 0.25F;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (client.getWindow().getWidth() / 2), (float) (client.getWindow().getHeight() / 2), 0.0F);
        GL11.glScalef(var8, var8, 0.0F);
        GL11.glTranslatef((float) (-client.getWindow().getWidth() / 2), (float) (-client.getWindow().getHeight() / 2), 0.0F);
        RenderUtils.drawImage((float) var6, (float) var7, (float) var4, (float) var5, sigmaLogo, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), bgOpacity));
        float var9 = Math.min(1.0F, var1 * 1.02F);
        float var11 = 80;
        if (bgOpacity == 1.0F) {
            RenderUtils.drawRoundedRect(
                    (float) var6, var7 + var5 + var11, (float) var4, 20.0F, 10.0F, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F * bgOpacity)
            );
            RenderUtils.drawRoundedRect(
                    (float) (var6 + 1),
                    var7 + var5 + var11 + 1,
                    (float) (var4 - 2),
                    18.0F,
                    9.0F,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), bgOpacity)
            );
        }

        RenderUtils.drawRoundedRect(
                (float) (var6 + 2),
                var7 + var5 + var11 + 2,
                (float) ((int) ((float) (var4 - 4) * var9)),
                16.0F,
                8.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F * bgOpacity)
        );
        GL11.glPopMatrix();
    }
}
