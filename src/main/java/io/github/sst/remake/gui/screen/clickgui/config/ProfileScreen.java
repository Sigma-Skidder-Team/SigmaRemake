package io.github.sst.remake.gui.screen.clickgui.config;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.profile.Profile;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.gui.screen.clickgui.ClickGuiScreen;
import io.github.sst.remake.manager.impl.ConfigManager;
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
    private static final int OUTER_PADDING = 10;
    private static final int HEADER_HEIGHT = 80;
    private static final int TITLE_Y_OFFSET = 20;
    private static final int TITLE_X_OFFSET = 25;
    private static final int DIVIDER_Y = 69;
    private static final int LIST_ENTRY_HEIGHT = 70;

    public final AnimationUtils openAnimation;

    public ScrollablePanel profileListScrollPanel;
    public OnlineProfilePanel onlineProfilePanel;

    private final List<ProfileListEntry> profileEntries = new ArrayList<>();

    public ProfileScreen(GuiComponent parent, String name, int x, int y) {
        super(parent, name, x - 250, y - 500, 250, 500, ColorHelper.DEFAULT_COLOR, false);

        this.openAnimation = new AnimationUtils(300, 100);

        this.setReAddChildren(true);
        this.setListening(false);

        TextButton addProfileButton = new TextButton(
                this,
                "addButton",
                this.width - 55,
                0,
                FontUtils.HELVETICA_LIGHT_25.getWidth("Add"),
                69,
                ColorHelper.DEFAULT_COLOR,
                "+",
                FontUtils.HELVETICA_LIGHT_25
        );
        addProfileButton.onClick((mouseX, mouseY) -> this.onlineProfilePanel.setExpanded(true));
        this.addToList(addProfileButton);

        this.onlineProfilePanel = new OnlineProfilePanel(this, "profile", 0, DIVIDER_Y, this.width, 200);
        this.onlineProfilePanel.setReAddChildren(true);
        this.addToList(this.onlineProfilePanel);

        this.reload();
    }

    public void duplicateSelectedProfile() {
        ConfigManager configManager = Client.INSTANCE.configManager;
        Profile selectedProfile = configManager.currentProfile;

        int copyIndex = 1;
        while (configManager.doesProfileExist(selectedProfile.name + " Copy " + copyIndex)) {
            copyIndex++;
        }

        configManager.saveProfile(selectedProfile.clone(selectedProfile.name + " Copy " + copyIndex), true);
        this.addRunnable(this::reload);
        this.onlineProfilePanel.setExpanded(false);
    }

    public void importProfile(Profile profile) {
        ConfigManager configManager = Client.INSTANCE.configManager;

        int suffixIndex = 1;
        while (configManager.doesProfileExist(profile.name + " " + suffixIndex)) {
            suffixIndex++;
        }

        configManager.saveProfile(profile.clone(profile.name + " " + suffixIndex), true);
        this.addRunnable(this::reload);
        this.onlineProfilePanel.setExpanded(false);
    }

    public void createBlankProfile() {
        ConfigManager configManager = Client.INSTANCE.configManager;

        int suffixIndex = 1;
        while (configManager.doesProfileExist("New Profile " + suffixIndex)) {
            suffixIndex++;
        }

        configManager.saveProfile(new Profile("New Profile " + suffixIndex, new JsonObject()), true);
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
        return this.openAnimation.getDirection() == AnimationUtils.Direction.FORWARDS
                && this.openAnimation.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (mouseY > this.onlineProfilePanel.getAbsoluteY() + this.onlineProfilePanel.getHeight()) {
            this.onlineProfilePanel.setExpanded(false);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void reload() {
        int previousScrollOffset = 0;
        if (this.profileListScrollPanel != null) {
            previousScrollOffset = this.profileListScrollPanel.getScrollOffset();
            this.removeChildren(this.profileListScrollPanel);
        }

        this.profileListScrollPanel = new ScrollablePanel(
                this,
                "profileScrollView",
                OUTER_PADDING,
                HEADER_HEIGHT,
                this.width - (OUTER_PADDING * 2),
                this.height - HEADER_HEIGHT - OUTER_PADDING
        );
        this.addToList(this.profileListScrollPanel);

        this.profileListScrollPanel.setScrollOffset(previousScrollOffset);

        this.profileEntries.clear();

        int index = 0;
        for (Profile profile : Client.INSTANCE.configManager.profiles) {
            ProfileListEntry entry = new ProfileListEntry(
                    this,
                    "profile" + index,
                    0,
                    LIST_ENTRY_HEIGHT * index,
                    this.profileListScrollPanel.getWidth(),
                    LIST_ENTRY_HEIGHT,
                    profile
            );
            this.profileListScrollPanel.addToList(entry);
            this.profileEntries.add(entry);
            index++;
        }

        ClickGuiScreen clickGuiScreen = (ClickGuiScreen) this.getParent();
        clickGuiScreen.updateSideBar();
    }

    public void updatePositions() {
        int yOffset = 0;
        for (ProfileListEntry entry : this.profileEntries) {
            entry.setY(yOffset);
            yOffset += entry.getHeight();
        }
    }

    @Override
    public void draw(float partialTicks) {
        float openPercent = this.openAnimation.calcPercent();

        this.updatePositions();

        float scale = VecUtils.interpolate(openPercent, 0.37, 1.48, 0.17, 0.99);
        if (this.openAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            scale = VecUtils.interpolate(openPercent, 0.38, 0.73, 0.0, 1.0);
        }

        float widgetScale = 0.8F + scale * 0.2F;
        this.setScale(widgetScale, widgetScale);

        this.setTranslateY((int) (this.width * 0.25F * (1.0F - scale)));
        this.setTranslateX((int) (this.width * 0.14F * (1.0F - scale)));

        super.applyScaleTransforms();
        super.applyTranslationTransforms();

        int backgroundColor = ColorHelper.applyAlpha(
                -723724,
                QuadraticEasing.easeOutQuad(openPercent, 0.0F, 1.0F, 1.0F)
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + OUTER_PADDING / 2),
                (float) (this.y + OUTER_PADDING / 2),
                (float) (this.width - OUTER_PADDING),
                (float) (this.height - OUTER_PADDING),
                35.0F,
                openPercent
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + OUTER_PADDING / 2),
                (float) (this.y + OUTER_PADDING / 2),
                (float) (this.x - OUTER_PADDING / 2 + this.width),
                (float) (this.y - OUTER_PADDING / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), openPercent * 0.25F)
        );
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                (float) OUTER_PADDING,
                backgroundColor
        );

        float onlinePanelPercent = this.onlineProfilePanel.expandAnimation.calcPercent();
        float listScale = 0.9F + (1.0F - VecUtils.interpolate(onlinePanelPercent, 0.0, 0.96, 0.69, 0.99)) * 0.1F;
        if (this.onlineProfilePanel.expandAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            listScale = 0.9F + (1.0F - VecUtils.interpolate(onlinePanelPercent, 0.61, 0.01, 0.87, 0.16)) * 0.1F;
        }

        this.profileListScrollPanel.setScale(listScale, listScale);

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.x + TITLE_X_OFFSET),
                (float) (this.y + TITLE_Y_OFFSET),
                "Profiles",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * openPercent)
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + TITLE_X_OFFSET),
                (float) (this.y + DIVIDER_Y),
                (float) (this.x + this.width - TITLE_X_OFFSET),
                (float) (this.y + DIVIDER_Y + 1),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * openPercent)
        );

        super.draw(openPercent);
    }
}