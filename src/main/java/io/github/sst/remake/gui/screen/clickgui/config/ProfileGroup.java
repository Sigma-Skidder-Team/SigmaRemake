package io.github.sst.remake.gui.screen.clickgui.config;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.panel.Widget;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.io.audio.SoundUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ProfileGroup extends Widget {
    public GuiComponent editButtons;
    public AnimationUtils hoverAnimation;
    public AnimationUtils slideAnimation;
    public AnimationUtils deleteAnimation;
    public Profile profile;
    public TextField profileName;
    public final int editButtonsWidth;
    public final int initialHeight;

    public ProfileGroup(GuiComponent parent, String name, int x, int y, int width, int height, Profile profile) {
        super(parent, name, x, y, width, height, false);
        this.editButtonsWidth = (int) ((float) width * 0.8F);
        this.initialHeight = height;
        this.profile = profile;

        ColorHelper renameButtonColors = ColorHelper.DEFAULT_COLOR.clone();
        renameButtonColors.setPrimaryColor(-11371052);
        renameButtonColors.setSecondaryColor(-12096331);
        renameButtonColors.setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        ColorHelper deleteButtonColors = ColorHelper.DEFAULT_COLOR.clone();
        deleteButtonColors.setPrimaryColor(-3254955);
        deleteButtonColors.setSecondaryColor(-4700859);
        deleteButtonColors.setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        this.addToList(this.editButtons = new EditButton(this, "edit", width - this.editButtonsWidth, 0, this.editButtonsWidth, height));
        ConfigButton renameButton;
        this.editButtons.addToList(renameButton = new ConfigButton(this.editButtons, "rename", 0, 0, this.editButtonsWidth / 2, height, renameButtonColors, "Rename"));
        ConfigButton deleteButton;
        this.editButtons.addToList(deleteButton = new ConfigButton(this.editButtons, "remove", this.editButtonsWidth / 2, 0, this.editButtonsWidth / 2, height, deleteButtonColors, "Delete"));
        this.editButtons.setHovered(false);
        ColorHelper textFieldColor = new ColorHelper(-892679478, -892679478, -892679478, ClientColors.DEEP_TEAL.getColor(), FontAlignment.LEFT, FontAlignment.CENTER);
        this.addToList(this.profileName = new TextField(this, "profileName", 16, 8, this.getWidth() - 60, 50, textFieldColor, profile.name));
        this.profileName.setRoundedThingy(false);
        this.profileName.setFont(FontUtils.HELVETICA_LIGHT_24);
        this.profileName.setSelfVisible(false);
        this.profileName.addKeyPressListener((character, key) -> {
            if (this.profileName.isFocused() && key == 257) {
                this.rename(this.profileName.getText());
            }
        });
        renameButton.setFont(FontUtils.HELVETICA_LIGHT_18);
        deleteButton.setFont(FontUtils.HELVETICA_LIGHT_18);
        renameButton.addWidthSetter((button, editButton) -> button.setWidth(Math.round((float) editButton.getWidth() / 2.0F)));
        deleteButton.addWidthSetter((button, editButton) -> {
            button.setX(Math.round((float) editButton.getWidth() / 2.0F));
            button.setWidth(Math.round((float) editButton.getWidth() / 2.0F));
        });
        deleteButton.onClick((mouseX, mouseY) -> {
            this.deleteAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            Client.INSTANCE.configManager.deleteProfile(this.profile);
            ConfigScreen configScreen = (ConfigScreen) this.getParent().getParent().getParent();
            configScreen.addRunnable(configScreen::reload);
        });
        renameButton.onClick((mouseX, mouseY) -> {
            this.slideAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
            this.profileName.setSelfVisible(true);
            this.profileName.startFocus();
        });
        this.editButtons.setWidth(0);
        this.editButtons.setTranslateX(this.editButtonsWidth);
        this.hoverAnimation = new AnimationUtils(100, 100, AnimationUtils.Direction.FORWARDS);
        this.slideAnimation = new AnimationUtils(290, 290, AnimationUtils.Direction.FORWARDS);
        this.deleteAnimation = new AnimationUtils(200, 100, AnimationUtils.Direction.FORWARDS);
        this.onClick((mouseX, mouseY) -> {
            if (mouseY != 1) {
                this.slideAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
                if (this.slideAnimation.calcPercent() == 0.0F) {
                    Client.INSTANCE.configManager.loadProfile(this.profile);
                    SoundUtils.play("switch");
                    ConfigScreen configScreen = (ConfigScreen) this.getParent().getParent().getParent();
                    configScreen.addRunnable(configScreen::reload);
                }
            } else {
                this.slideAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            }
        });
    }

    public void rename(String newName) {
        this.profileName.setSelfVisible(false);
        this.profileName.setFocused(false);
        Client.INSTANCE.configManager.renameProfile(this.profile, newName);
        ConfigScreen configScreen = (ConfigScreen) this.getParent().getParent().getParent();
        configScreen.addRunnable(configScreen::reload);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!this.profileName.isFocused() && this.profileName.isSelfVisible()) {
            this.rename(this.profileName.getText());
        }

        this.hoverAnimation.changeDirection(this.isMouseOverComponent(mouseX, mouseY) ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        if (!this.isMouseOverComponent(mouseX, mouseY)) {
            this.slideAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float deleteAnimationPercentage = VecUtils.interpolate(this.deleteAnimation.calcPercent(), 0.1, 0.81, 0.14, 1.0);
        this.setHeight(Math.round((1.0F - deleteAnimationPercentage) * (float) this.initialHeight));
        partialTicks *= 1.0F - this.deleteAnimation.calcPercent();
        float slideAnimationPercentage = VecUtils.interpolate(this.slideAnimation.calcPercent(), 0.28, 1.26, 0.33, 1.04);
        if (this.slideAnimation.getDirection().equals(AnimationUtils.Direction.FORWARDS)) {
            slideAnimationPercentage = AnimationUtils.calculateBackwardTransition(this.slideAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        this.editButtons.setHovered(this.slideAnimation.calcPercent() == 1.0F);
        this.editButtons.setWidth(Math.max(0, (int) ((float) this.editButtonsWidth * slideAnimationPercentage)));
        this.editButtons.setTranslateX((int) ((float) this.editButtonsWidth * (1.0F - slideAnimationPercentage)));
        ScissorUtils.startScissor(this);
        float hoverPercentage = this.isMouseDownOverComponent() && this.slideAnimation.getDirection().equals(AnimationUtils.Direction.FORWARDS) ? 0.03F : 0.0F;
        RenderUtils.drawRoundedRect2(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.04F * this.hoverAnimation.calcPercent() + hoverPercentage)
        );
        if (!this.profileName.isFocused()) {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_24,
                    (float) (this.x + 20) - slideAnimationPercentage * (float) this.width,
                    (float) (this.y + 18),
                    this.profile.name,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.9F * partialTicks)
            );
        }

        this.profileName.setTranslateX(Math.round(-slideAnimationPercentage * (float) this.width));
        if (Client.INSTANCE.configManager.currentProfile == this.profile) {
            RenderUtils.drawImage(
                    (float) (this.getX() + this.getWidth() - 35) - slideAnimationPercentage * (float) this.width,
                    (float) (this.getY() + 27),
                    17.0F,
                    13.0F,
                    Resources.activePNG,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), (1.0F - this.slideAnimation.calcPercent()) * partialTicks)
            );
        }

        super.draw(partialTicks);
        ScissorUtils.restoreScissor();
    }
}
