package io.github.sst.remake.gui.screen.mainmenu;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.screen.loading.LoadingScreen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.java.StringUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenuScreen extends Screen implements IMinecraft {
    private final AnimationUtils changelogTransitionAnimation = new AnimationUtils(200, 200, AnimationUtils.Direction.FORWARDS);
    private final AnimationUtils goodbyeAnimation = new AnimationUtils(200, 200, AnimationUtils.Direction.FORWARDS);
    private final AnimationUtils backgroundFadeAnimation = new AnimationUtils(325, 325);
    private final AnimationUtils foregroundFadeAnimation = new AnimationUtils(800, 800);
    private final List<FloatingBubble> bubbles = new ArrayList<>();

    private static long currentTime = System.nanoTime();
    private int backgroundParallaxY = 0;
    private int backgroundParallaxX = 0;
    private boolean firstParallaxUpdate = true;

    private final MainPage mainMenuScreen;
    private final ChangelogPage changelogPage;

    public float deltaTime;

    public MainMenuScreen() {
        super("Main Screen");
        this.setListening(false);

        this.backgroundFadeAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.foregroundFadeAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);

        Random random = new Random();

        for (int i = 0; i < client.getWindow().getWidth() * client.getWindow().getHeight() / 14000; i++) {
            int x = random.nextInt(client.getWindow().getWidth());
            int y = random.nextInt(client.getWindow().getHeight());
            int radius = 7 + random.nextInt(5);
            int velocityX = (1 + random.nextInt(4)) * (!random.nextBoolean() ? 1 : -1);
            int velocityY = 1 + random.nextInt(2);
            this.bubbles.add(new FloatingBubble(this, Integer.toString(i), x, y, radius, velocityX, velocityY));
        }

        this.addToList(this.mainMenuScreen = new MainPage(this, "main", 0, 0, this.width, this.height));
        this.addToList(this.changelogPage = new ChangelogPage(this, "changelog", 0, 0, this.width, this.height));

        this.changelogPage.setHovered(false);
        this.changelogPage.setBringToFront(true);
    }

    public void hideChangelog() {
        this.changelogTransitionAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.changelogPage.setHovered(false);
    }

    public void startQuitAnimation() {
        this.changelogTransitionAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        this.goodbyeAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
    }

    public void showChangelog() {
        this.changelogTransitionAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        this.changelogPage.setHovered(true);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        for (GuiComponent var6 : this.bubbles) {
            var6.updatePanelDimensions(mouseX, mouseY);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float transitionProgress = AnimationUtils.easeOutCubic(this.changelogTransitionAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.changelogTransitionAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            transitionProgress = AnimationUtils.easeInCubic(this.changelogTransitionAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        float scaleOffset = 0.07F * transitionProgress;
        this.mainMenuScreen.setScale(1.0F - scaleOffset, 1.0F - scaleOffset);
        this.mainMenuScreen.setHovered(this.changelogTransitionAnimation.calcPercent() == 0.0F);
        long elapsedTime = System.nanoTime() - currentTime;
        deltaTime = Math.min(10.0F, Math.max(0.0F, (float) elapsedTime / 1.810361E7F / 2.0F));
        currentTime = System.nanoTime();
        int offsetX = -this.getMouseX();
        float offsetY = (float) this.getMouseY() / (float) this.getWidth() * -114.0F;
        if (this.firstParallaxUpdate) {
            this.backgroundParallaxY = (int) offsetY;
            this.backgroundParallaxX = offsetX;
            this.firstParallaxUpdate = false;
        }

        float deltaX = offsetY - (float) this.backgroundParallaxY;
        float deltaY = (float) (offsetX - this.backgroundParallaxX);
        if (client.overlay != null) {
            if (offsetY != (float) this.backgroundParallaxY) {
                this.backgroundParallaxY = (int) ((float) this.backgroundParallaxY + deltaX * deltaTime);
            }

            if (offsetX != this.backgroundParallaxX) {
                this.backgroundParallaxX = (int) ((float) this.backgroundParallaxX + deltaY * deltaTime);
            }
        } else {
            this.backgroundFadeAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            this.foregroundFadeAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            float parallaxFactor = 0.5F - (float) this.backgroundParallaxX / (float) client.getWindow().getWidth() * -1.0F;
            float backgroundOpacity = 1.0F - this.backgroundFadeAnimation.calcPercent();
            float foregroundOpacity = 1.0F - this.foregroundFadeAnimation.calcPercent();

            float screenScale = (float) this.getWidth() / 1920.0F;
            int backgroundWidth = (int) (600.0F * screenScale);
            int middleWidth = (int) (450.0F * screenScale);
            int foregroundWidth = 0;

            RenderUtils.drawImage(
                    (float) this.backgroundParallaxX - (float) backgroundWidth * parallaxFactor,
                    (float) this.backgroundParallaxY,
                    (float) (this.getWidth() * 2 + backgroundWidth),
                    (float) (this.getHeight() + 114),
                    Resources.MENU_BACKGROUND
            );
            RenderUtils.drawImage(
                    (float) this.backgroundParallaxX - (float) middleWidth * parallaxFactor,
                    (float) this.backgroundParallaxY,
                    (float) (this.getWidth() * 2 + middleWidth),
                    (float) (this.getHeight() + 114),
                    Resources.MENU_MIDDLEGROUND
            );

            for (GuiComponent bubble : this.bubbles) {
                GL11.glPushMatrix();
                bubble.draw(partialTicks);
                GL11.glPopMatrix();
            }

            RenderUtils.drawImage(
                    (float) this.backgroundParallaxX - (float) foregroundWidth * parallaxFactor,
                    (float) this.backgroundParallaxY,
                    (float) (this.getWidth() * 2 + foregroundWidth),
                    (float) (this.getHeight() + 114),
                    Resources.MENU_FOREGROUND
            );

            // blurs background when changelog animation
            RenderUtils.drawImage(
                    (float) this.backgroundParallaxX,
                    (float) (this.backgroundParallaxY - 50),
                    (float) (this.getWidth() * 2),
                    (float) (this.getHeight() + 200),
                    Resources.MENU_PANORAMA_2,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), transitionProgress),
                    false
            );

            //darkness effect
            RenderUtils.drawRoundedRect2(
                    0.0F,
                    0.0F,
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), transitionProgress * 0.3F)
            );

            for (GuiComponent object : this.getChildren()) {
                if (object.isSelfVisible()) {
                    GL11.glPushMatrix();

                    if (object instanceof ChangelogPage) {
                        if (transitionProgress > 0.0F) {
                            object.draw(partialTicks);
                        }
                    } else {
                        object.draw(partialTicks * (1.0F - transitionProgress));
                    }

                    GL11.glPopMatrix();
                }
            }

            if (foregroundOpacity > 0.0F && !Client.INSTANCE.loaded) {
                RenderUtils.renderFadeOut(backgroundOpacity, 1.0F);
                Client.INSTANCE.loaded = true;
            }

            deltaTime *= 0.7F;
            deltaTime = Math.min(deltaTime, 1.0F);
            if (!this.firstParallaxUpdate && (foregroundOpacity == 0.0F || this.backgroundParallaxY != 0 || this.backgroundParallaxX != 0)) {
                if (offsetY != (float) this.backgroundParallaxY) {
                    this.backgroundParallaxY = (int) ((float) this.backgroundParallaxY + deltaX * deltaTime);
                }

                if (offsetX != this.backgroundParallaxX) {
                    this.backgroundParallaxX = (int) ((float) this.backgroundParallaxX + deltaY * deltaTime);
                }
            }

            if (this.goodbyeAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_MEDIUM_50,
                        (float) (this.width / 2),
                        (float) (this.height / 2 - 30),
                        StringUtils.RANDOM_GOODBYE_TITLE,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.goodbyeAnimation.calcPercent()),
                        FontAlignment.CENTER,
                        FontAlignment.CENTER
                );
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_18,
                        (float) (this.width / 2),
                        (float) (this.height / 2 + 30),
                        "\"" + StringUtils.RANDOM_GOODBYE_MESSAGE + "\"",
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.goodbyeAnimation.calcPercent() * 0.5F),
                        FontAlignment.CENTER,
                        FontAlignment.CENTER
                );
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) { //escape key
            this.hideChangelog();
        }
    }
}
