package io.github.sst.remake.gui.screen.clickgui.config;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.gui.screen.clickgui.ClickGuiScreen;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.manager.impl.ConfigManager;
import io.github.sst.remake.data.profile.Profile;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfileScreen extends InteractiveWidget {
    public final AnimationUtils openAnimation;
    public ScrollablePanel profileScrollView;
    public OnlineProfilePanel onlineProfilePanel;
    private final List<ProfileListEntry> profileListEntries = new ArrayList<>();

    public ProfileScreen(GuiComponent parent, String name, int x, int y) {
        super(parent, name, x - 250, y - 500, 250, 500, ColorHelper.DEFAULT_COLOR, false);
        this.openAnimation = new AnimationUtils(300, 100);
        this.setReAddChildren(true);
        this.setListening(false);
        TextButton addButton = new TextButton(this, "addButton", this.width - 55, 0, FontUtils.HELVETICA_LIGHT_25.getWidth("Add"), 69, ColorHelper.DEFAULT_COLOR, "+", FontUtils.HELVETICA_LIGHT_25);
        addButton.onClick((mouseX, mouseY) -> this.onlineProfilePanel.setExpanded(true));
        this.addToList(addButton);
        this.onlineProfilePanel = new OnlineProfilePanel(this, "profile", 0, 69, this.width, 200);
        this.onlineProfilePanel.setReAddChildren(true);
        this.addToList(onlineProfilePanel);
        this.reload();
    }

    public void duplicateSelectedProfile() {
        ConfigManager configManager = Client.INSTANCE.configManager;
        Profile currentProfile = configManager.currentProfile;
        int i = 1;

        while (configManager.doesProfileExist(currentProfile.name + " Copy " + i)) {
            i++;
        }

        configManager.saveProfile(currentProfile.clone(currentProfile.name + " Copy " + i), true);
        this.addRunnable(this::reload);
        this.onlineProfilePanel.setExpanded(false);
    }

    public void importProfile(Profile profile) {
        ConfigManager configManager = Client.INSTANCE.configManager;
        int i = 1;

        while (configManager.doesProfileExist(profile.name + " " + i)) {
            i++;
        }

        configManager.saveProfile(profile.clone(profile.name + " " + i), true);
        this.addRunnable(this::reload);
        this.onlineProfilePanel.setExpanded(false);
    }

    public void createBlankProfile() {
        ConfigManager configManager = Client.INSTANCE.configManager;
        int i = 1;

        while (configManager.doesProfileExist("New Profile " + i)) {
            i++;
        }

        configManager.saveProfile(new Profile("New Profile " + i, new JsonObject()), true);
        this.addRunnable(this::reload);
        this.onlineProfilePanel.setExpanded(false);
    }

    public void close() {
        this.onlineProfilePanel.expandAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        if (this.openAnimation.getDirection() != AnimationUtils.Direction.FORWARDS) {
            this.openAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        }
    }

    public boolean isClosed() {
        return this.openAnimation.getDirection() == AnimationUtils.Direction.FORWARDS && this.openAnimation.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (mouseY > this.onlineProfilePanel.getAbsoluteY() + this.onlineProfilePanel.getHeight()) {
            this.onlineProfilePanel.setExpanded(false);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void reload() {
        int scrollOffset = 0;
        if (this.profileScrollView != null) {
            scrollOffset = this.profileScrollView.getScrollOffset();
            this.removeChildren(this.profileScrollView);
        }

        this.addToList(this.profileScrollView = new ScrollablePanel(this, "profileScrollView", 10, 80, this.width - 20, this.height - 80 - 10));
        this.profileScrollView.setScrollOffset(scrollOffset);
        this.profileListEntries.clear();

        int i = 0;
        int height = 70;

        for (Profile profile : Client.INSTANCE.configManager.profiles) {
            ProfileListEntry profileListEntry = new ProfileListEntry(this, "profile" + i, 0, height * i, this.profileScrollView.getWidth(), height, profile);
            this.profileScrollView.addToList(profileListEntry);
            this.profileListEntries.add(profileListEntry);
            i++;
        }

        ClickGuiScreen clickGuiScreen = (ClickGuiScreen) this.getParent();
        clickGuiScreen.updateSideBar();
    }

    public void updatePositions() {
        int y = 0;

        for (ProfileListEntry profileListEntry : this.profileListEntries) {
            profileListEntry.setY(y);
            y += profileListEntry.getHeight();
        }
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.openAnimation.calcPercent();
        this.updatePositions();
        float scale = VecUtils.interpolate(partialTicks, 0.37, 1.48, 0.17, 0.99);
        if (this.openAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            scale = VecUtils.interpolate(partialTicks, 0.38, 0.73, 0.0, 1.0);
        }

        this.setScale(0.8F + scale * 0.2F, 0.8F + scale * 0.2F);
        this.setTranslateY((int) ((float) this.width * 0.25F * (1.0F - scale)));
        this.setTranslateX((int) ((float) this.width * 0.14F * (1.0F - scale)));
        super.applyScaleTransforms();
        super.applyTranslationTransforms();
        int padding = 10;
        int color = ColorHelper.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.width - padding),
                (float) (this.height - padding),
                35.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.x - padding / 2 + this.width),
                (float) (this.y - padding / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtils.drawRoundedRect((float) this.x, (float) this.y, (float) this.width, (float) this.height, (float) padding, color);
        float profileViewScale = 0.9F + (1.0F - VecUtils.interpolate(this.onlineProfilePanel.expandAnimation.calcPercent(), 0.0, 0.96, 0.69, 0.99)) * 0.1F;
        if (this.onlineProfilePanel.expandAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            profileViewScale = 0.9F + (1.0F - VecUtils.interpolate(this.onlineProfilePanel.expandAnimation.calcPercent(), 0.61, 0.01, 0.87, 0.16)) * 0.1F;
        }

        this.profileScrollView.setScale(profileViewScale, profileViewScale);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.x + 25),
                (float) (this.y + 20),
                "Profiles",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 69),
                (float) (this.x + this.width - 25),
                (float) (this.y + 70),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        super.draw(partialTicks);
    }
}
