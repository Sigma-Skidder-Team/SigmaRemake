package io.github.sst.remake.gui.impl;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.SmallImage;
import io.github.sst.remake.gui.element.impl.cgui.SettingGroup;
import io.github.sst.remake.gui.element.impl.cgui.config.ConfigScreen;
import io.github.sst.remake.gui.element.impl.cgui.overlay.BlurOverlay;
import io.github.sst.remake.gui.element.impl.cgui.overlay.BrainFreezeOverlay;
import io.github.sst.remake.gui.element.impl.cgui.CategoryPanel;
import io.github.sst.remake.gui.screen.holder.ClickGuiHolder;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class JelloScreen extends Screen implements IMinecraft {
    public BlurOverlay blurOverlay;
    private static AnimationUtils animationProgress;
    private static boolean animationStarted;
    private static boolean animationCompleted;
    private final Map<Category, CategoryPanel> categoryPanels = new HashMap<>();
    //public MusicPlayer musicPlayer;
    public BrainFreezeOverlay brainFreeze;
    public ConfigScreen configButton;
    public SettingGroup settingGroup;
    public CategoryPanel categoryPanel = null;

    public JelloScreen() {
        super("JelloScreen");
        animationCompleted = animationCompleted | !animationStarted;
        int x = 30;
        int y = 30;
        this.addToList(this.brainFreeze = new BrainFreezeOverlay(this, "brainFreeze"));

        for (Module module : Client.INSTANCE.moduleManager.modules) {
            if (!this.categoryPanels.containsKey(module.getCategory())) {
                CategoryPanel categoryPanel = new CategoryPanel(this, x, y, module.getCategory());
                this.categoryPanels.put(module.getCategory(), categoryPanel);
                this.addToList(categoryPanel);

                x += categoryPanel.getWidth() + 10;
                if (this.categoryPanels.size() == 4) {
                    x = 30;
                    y += categoryPanel.getHeight() - 20;
                }

                categoryPanel.method13507(var2 -> this.addRunnable(() -> {
                    this.addToList(this.settingGroup = new SettingGroup(this, "settings", 0, 0, this.width, this.height, var2));
                    this.settingGroup.setReAddChildren(true);
                }));
            }
        }

        //this.addToList(this.musicPlayer = new MusicPlayer(this, "musicPlayer"));
        //this.musicPlayer.method13215(true);
        SmallImage moreButton;
        this.addToList(moreButton = new SmallImage(this, "more", this.getWidth() - 69, this.getHeight() - 55, 55, 41, Resources.optionsPNG1));

        moreButton.getTextColor().setPrimaryColor(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F));
        moreButton.setListening(false);

        //this.musicPlayer.setSelfVisible(true);

        moreButton.onClick((var1, var2) -> this.addRunnable(() -> {
            if (this.configButton != null && this.hasChild(this.configButton)) {
                this.queueChildRemoval(this.configButton);
            } else {
                this.addToList(this.configButton = new ConfigScreen(this, "morepopover", this.getWidth() - 14, this.getHeight() - 14));
                this.configButton.setReAddChildren(true);
            }
        }));

        animationProgress = new AnimationUtils(450, 125);
        this.blurOverlay = new BlurOverlay(this, this, "overlay");
        ShaderUtils.applyBlurShader();
        ShaderUtils.setShaderRadiusRounded(animationProgress.calcPercent());
    }

    public void method13315() {
        for (CategoryPanel panel : this.categoryPanels.values()) {
            panel.method13504();
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        //this.musicPlayer.setSelfVisible(this.musicPlayer.getWidth() < this.getWidth() && this.musicPlayer.getHeight() < this.getHeight());
        super.updatePanelDimensions(mouseX, mouseY);
        ShaderUtils.setShaderRadiusRounded(Math.min(1.0F, animationProgress.calcPercent() * 4.0F));
        this.brainFreeze.setSelfVisible(Client.INSTANCE.moduleManager.getModule("BrainFreeze").enabled);

        if (this.configButton != null) {
            int newHeightValue = mouseX - this.configButton.getAbsoluteX();
            int newWidthValue = mouseY - this.configButton.getAbsoluteY();
            boolean conditionMet = newHeightValue >= -10 && newWidthValue >= -10;
            if (!conditionMet) {
                this.configButton.method13613();
            }
        }

        if (this.configButton != null && this.configButton.method13614()) {
            this.removeChildren(this.configButton);
            this.configButton = null;
        }

        if (animationProgress.getDirection() == AnimationUtils.Direction.BACKWARDS && this.settingGroup != null && !this.settingGroup.field20671) {
            this.settingGroup.field20671 = true;
        }

        if (this.settingGroup != null && this.settingGroup.field20671 && this.settingGroup.animation1.calcPercent() == 0.0F) {
            this.addRunnable(() -> {
                this.removeChildren(this.settingGroup);
                this.settingGroup = null;
            });
        }

        if (animationCompleted) {
            AnimationUtils.Direction direction = animationProgress.getDirection();
            animationProgress.changeDirection(!animationStarted ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);

            if (animationProgress.calcPercent() <= 0.0F && animationStarted) {
                animationStarted = false;
                this.handleAnimationCompletion(animationStarted);
            } else if (animationProgress.calcPercent() >= 1.0F && animationProgress.getDirection() == direction) {
                animationStarted = true;
                this.handleAnimationCompletion(animationStarted);
            }
        }

        if (animationCompleted && animationStarted) {
            ShaderUtils.resetShader();
        }
    }

    @Override
    public int getFPS() {
        return MinecraftClient.currentFps;
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        ShaderUtils.resetShader();
        this.queueChildRemoval(this.blurOverlay);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);
    }

    private void handleAnimationCompletion(boolean started) {
        animationCompleted = false;
        if (!started) {
            client.openScreen(null);
        }
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton <= 1) {
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        } else {
            this.keyPressed(mouseButton);
            return false;
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int keyBindForClickGui = Client.INSTANCE.bindManager.getKeybindFor(ClickGuiHolder.class);
        if (keyCode == 256 || keyCode == keyBindForClickGui && this.settingGroup == null && !this.hasFocusedTextField()) {
            if (animationCompleted) {
                animationStarted = !animationStarted;
            }

            animationCompleted = true;
        }
    }

    public float method13317(float var1, float var2) {
        return animationProgress.getDirection() != AnimationUtils.Direction.BACKWARDS
                ? (float) (Math.pow(2.0, -10.0F * var1) * Math.sin((double) (var1 - var2 / 4.0F) * (Math.PI * 2) / (double) var2) + 1.0)
                : QuadraticEasing.easeOutQuad(var1, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float alphaFactor = animationCompleted && !animationStarted
                ? this.method13317(animationProgress.calcPercent(), 0.8F) * 0.5F + 0.5F
                : (!animationCompleted ? 1.0F : this.method13317(animationProgress.calcPercent(), 1.0F));
        float alpha = 0.2F * partialTicks * alphaFactor;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) (this.y + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha)
        );
        float fadeAmount = 1.0F;
        if (this.settingGroup != null) {
            float var8 = EasingFunctions.easeOutBack(this.settingGroup.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
            if (this.settingGroup.animation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
                var8 = AnimationUtils.calculateBackwardTransition(this.settingGroup.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
            }

            fadeAmount -= this.settingGroup.animation.calcPercent() * 0.1F;
            alphaFactor *= 1.0F + var8 * 0.2F;
        }

        if (Client.INSTANCE.configManager.profile != null && !Client.INSTANCE.notificationManager.isRendering()) {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_20,
                    (float) (this.width - FontUtils.HELVETICA_LIGHT_20.getWidth(Client.INSTANCE.configManager.profile.name) - 80),
                    (float) (this.height - 47),
                    Client.INSTANCE.configManager.profile.name,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * Math.max(0.0F, Math.min(1.0F, alphaFactor)))
            );
        }

        for (CustomGuiScreen child : this.getChildren()) {
            float x = (float) (child.getX() + child.getWidth() / 2 - client.getWindow().getWidth() / 2) * (1.0F - alphaFactor) * 0.5F;
            float y = (float) (child.getY() + child.getHeight() / 2 - client.getWindow().getHeight() / 2) * (1.0F - alphaFactor) * 0.5F;
            child.setTranslate((int) x, (int) y);
            child.setScale(1.5F - alphaFactor * 0.5F, 1.5F - alphaFactor * 0.5F);
        }

        super.draw(partialTicks * Math.min(1.0F, alphaFactor) * fadeAmount);
        if (this.categoryPanel != null) {
            this.categoryPanel.setReAddChildren(false);
        }

        this.blurOverlay.setReAddChildren(false);
        this.queueChildRemoval(this.blurOverlay);
    }
}
