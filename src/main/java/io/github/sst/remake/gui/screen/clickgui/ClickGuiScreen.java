package io.github.sst.remake.gui.screen.clickgui;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.framework.widget.Alert;
import io.github.sst.remake.gui.framework.widget.Image;
import io.github.sst.remake.gui.framework.widget.internal.AlertComponent;
import io.github.sst.remake.gui.framework.widget.internal.ComponentType;
import io.github.sst.remake.gui.screen.clickgui.config.ProfileScreen;
import io.github.sst.remake.gui.screen.clickgui.overlay.BrainFreezeOverlay;
import io.github.sst.remake.gui.screen.holder.ClickGuiHolder;
import io.github.sst.remake.gui.screen.musicplayer.MusicPlayer;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.gui.BrainFreezeModule;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import io.github.sst.remake.util.system.VersionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen implements IMinecraft {
    private static AnimationUtils openCloseAnimation;
    private static boolean openState;
    private static boolean animationActive;
    private final Map<Category, CategoryPanel> categoryPanels = new HashMap<>();
    public MusicPlayer musicPlayer;
    public ProfileScreen profileScreen;
    public BrainFreezeOverlay brainFreeze;
    public ModuleSettingsDialog moduleSettingsDialog;
    public CategoryPanel categoryPanel = null;
    public Alert dependencyAlert;

    public ClickGuiScreen() {
        super("JelloScreen");
        animationActive = animationActive | !openState;
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

                categoryPanel.addModuleClickListener(var2 -> this.addRunnable(() -> {
                    this.addToList(this.moduleSettingsDialog = new ModuleSettingsDialog(this, "settings", 0, 0, this.width, this.height, var2));
                    this.moduleSettingsDialog.setReAddChildren(true);
                }));
            }
        }

        this.addToList(this.musicPlayer = new MusicPlayer(this, "musicPlayer"));
        this.musicPlayer.setDraggable(true);
        this.musicPlayer.setSelfVisible(true);

        Image moreButton;
        this.addToList(moreButton = new Image(this, "more", this.getWidth() - 69, this.getHeight() - 55, 55, 41, Resources.MORE_ICON));

        moreButton.getTextColor().setPrimaryColor(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F));
        moreButton.setListening(false);
        moreButton.onClick((parent, mouseButton) -> this.addRunnable(() -> {
            if (this.profileScreen != null && this.hasChild(this.profileScreen)) {
                this.queueChildRemoval(this.profileScreen);
            } else {
                this.addToList(this.profileScreen = new ProfileScreen(this, "morepopover", this.getWidth() - 14, this.getHeight() - 14));
                this.profileScreen.setReAddChildren(true);
            }
        }));

        openCloseAnimation = new AnimationUtils(450, 125);
        ShaderUtils.applyBlurShader();
        ShaderUtils.setShaderRadiusRounded(openCloseAnimation.calcPercent());
    }

    public void updateSideBar() {
        for (CategoryPanel panel : this.categoryPanels.values()) {
            panel.rebuildModuleList();
        }
    }

    public boolean checkMusicPlayerDependencies() {
        boolean hasPy = VersionUtils.hasPython3_11();
        boolean hasFF = VersionUtils.hasFFMPEG();

        if (!hasPy || !hasFF && dependencyAlert == null) {
            addRunnable(() -> {
                List<AlertComponent> components = new ArrayList<>();
                components.add(new AlertComponent(ComponentType.HEADER, "Music", 40));
                components.add(new AlertComponent(ComponentType.FIRST_LINE, "Jello Music requires:", 20));
                if (!hasPy)
                    components.add(new AlertComponent(ComponentType.FIRST_LINE, "- Python 3.11+", 30));
                if (!hasFF)
                    components.add(new AlertComponent(ComponentType.FIRST_LINE, "- FFMPEG", 30));

                components.add(new AlertComponent(ComponentType.BUTTON, "Download", 55));
                showAlert(dependencyAlert = new Alert(this, "music", true, "Dependencies.", components.toArray(new AlertComponent[0])));
                dependencyAlert.onPress(interactiveWidget -> {
                    if (!hasPy)
                        Util.getOperatingSystem().open("https://www.python.org/downloads/release/python-31114/");

                    if (!hasFF)
                        Util.getOperatingSystem().open("https://ffmpeg.org/download.html");
                });

                dependencyAlert.addCloseListener(thread -> new Thread(() -> addRunnable(() -> {
                    removeChildren(dependencyAlert);
                    dependencyAlert = null;
                })).start());

                dependencyAlert.setOpen(true);
            });

            return false;
        }

        return true;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.musicPlayer.setSelfVisible(this.musicPlayer.getWidth() < this.getWidth() && this.musicPlayer.getHeight() < this.getHeight());
        super.updatePanelDimensions(mouseX, mouseY);
        ShaderUtils.setShaderRadiusRounded(Math.min(1.0F, openCloseAnimation.calcPercent() * 4.0F));
        brainFreeze.setSelfVisible(Client.INSTANCE.moduleManager.getModule(BrainFreezeModule.class).enabled);

        if (profileScreen != null) {
            int xOver = mouseX - profileScreen.getAbsoluteX();
            int yOver = mouseY - profileScreen.getAbsoluteY();
            boolean conditionMet = xOver >= -10 && yOver >= -10;
            if (!conditionMet) {
                profileScreen.close();
            }
        }

        if (profileScreen != null && profileScreen.isClosed()) {
            removeChildren(profileScreen);
            profileScreen = null;
        }

        if (openCloseAnimation.getDirection() == AnimationUtils.Direction.FORWARDS && moduleSettingsDialog != null && !moduleSettingsDialog.closing) {
            moduleSettingsDialog.closing = true;
        }

        if (moduleSettingsDialog != null && moduleSettingsDialog.closing && moduleSettingsDialog.openScaleAnimation.calcPercent() == 0.0F) {
            addRunnable(() -> {
                removeChildren(moduleSettingsDialog);
                moduleSettingsDialog = null;
            });
        }

        if (animationActive) {
            AnimationUtils.Direction direction = openCloseAnimation.getDirection();
            openCloseAnimation.changeDirection(!openState ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);

            if (openCloseAnimation.calcPercent() <= 0.0F && openState) {
                openState = false;
                handleAnimationCompletion(openState);
            } else if (openCloseAnimation.calcPercent() >= 1.0F && openCloseAnimation.getDirection() == direction) {
                openState = true;
                handleAnimationCompletion(openState);
            }
        }

        if (animationActive && openState) {
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
        return super.toConfigWithExtra(config);
    }

    private void handleAnimationCompletion(boolean started) {
        animationActive = false;
        if (!started) {
            client.openScreen(null);
        }
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton <= 1) {
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        } else {
            keyPressed(mouseButton);
            return false;
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int keyBindForClickGui = Client.INSTANCE.bindManager.getKeybindFor(ClickGuiHolder.class);
        if (keyCode == keyBindForClickGui && animationActive) {
            return;
        }
        if (keyCode == 256 || keyCode == keyBindForClickGui && this.moduleSettingsDialog == null && !this.hasFocusedTextField()) {
            if (animationActive) {
                openState = !openState;
            }

            animationActive = true;
        }
    }

    public float calcOpenEasing(float var1, float var2) {
        return openCloseAnimation.getDirection() != AnimationUtils.Direction.FORWARDS
                ? (float) (Math.pow(2.0, -10.0F * var1) * Math.sin((double) (var1 - var2 / 4.0F) * (Math.PI * 2) / (double) var2) + 1.0)
                : QuadraticEasing.easeOutQuad(var1, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float alphaFactor = animationActive && !openState
                ? this.calcOpenEasing(openCloseAnimation.calcPercent(), 0.8F) * 0.5F + 0.5F
                : (!animationActive ? 1.0F : this.calcOpenEasing(openCloseAnimation.calcPercent(), 1.0F));
        float alpha = 0.2F * partialTicks * alphaFactor;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) (this.y + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha)
        );
        float fadeAmount = 1.0F;
        if (this.moduleSettingsDialog != null) {
            float var8 = EasingFunctions.easeOutBack(this.moduleSettingsDialog.fadeAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
            if (this.moduleSettingsDialog.fadeAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
                var8 = AnimationUtils.calculateBackwardTransition(this.moduleSettingsDialog.fadeAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
            }

            fadeAmount -= this.moduleSettingsDialog.fadeAnimation.calcPercent() * 0.1F;
            alphaFactor *= 1.0F + var8 * 0.2F;
        }

        if (Client.INSTANCE.configManager.currentProfile != null && !Client.INSTANCE.notificationManager.isRendering()) {
            String configName = Client.INSTANCE.configManager.currentProfile.name;
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_20,
                    (float) (this.width - FontUtils.HELVETICA_LIGHT_20.getWidth(configName) - 80),
                    (float) (this.height - 47),
                    configName,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * Math.max(0.0F, Math.min(1.0F, alphaFactor)))
            );
        }

        for (GuiComponent child : this.getChildren()) {
            float x = (float) (child.getX() + child.getWidth() / 2 - client.getWindow().getWidth() / 2) * (1.0F - alphaFactor) * 0.5F;
            float y = (float) (child.getY() + child.getHeight() / 2 - client.getWindow().getHeight() / 2) * (1.0F - alphaFactor) * 0.5F;
            child.setTranslate((int) x, (int) y);
            child.setScale(1.5F - alphaFactor * 0.5F, 1.5F - alphaFactor * 0.5F);
        }

        super.draw(partialTicks * Math.min(1.0F, alphaFactor) * fadeAmount);
        if (this.categoryPanel != null) {
            this.categoryPanel.setReAddChildren(false);
        }
    }
}
