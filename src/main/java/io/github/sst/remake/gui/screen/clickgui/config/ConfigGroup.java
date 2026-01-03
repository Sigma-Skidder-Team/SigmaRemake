package io.github.sst.remake.gui.screen.clickgui.config;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.gui.element.impl.alert.LoadingIndicator;
import io.github.sst.remake.gui.framework.layout.GridLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.manager.impl.OnlineProfileManager;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ConfigGroup extends InteractiveWidget {
    public AnimationUtils expandAnimation = new AnimationUtils(300, 200, AnimationUtils.Direction.FORWARDS);
    private final int maxHeight;
    private final ScrollablePanel profileList;
    private final OnlineProfileManager onlineProfileManager;
    private final LoadingIndicator loadingIndicator;

    public ConfigGroup(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, 0, ColorHelper.DEFAULT_COLOR, false);
        this.maxHeight = height;

        TextButton blankButton = new TextButton(this, "blankButton", 25, 0, FontUtils.HELVETICA_LIGHT_20.getWidth("Blank"), 30, ColorHelper.DEFAULT_COLOR, "Blank", FontUtils.HELVETICA_LIGHT_20);

        blankButton.onClick((var1x, var2x) -> {
            ConfigScreen configScreen = (ConfigScreen) this.getParent();
            configScreen.createBlankProfile();
        });

        TextButton duplicateButton = new TextButton(this, "dupeButton", width - 25 - FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"), 0, FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"), 30, ColorHelper.DEFAULT_COLOR, "Duplicate", FontUtils.HELVETICA_LIGHT_20);

        duplicateButton.onClick((var1x, var2x) -> {
            ConfigScreen configScreen = (ConfigScreen) this.getParent();
            configScreen.duplicateSelectedProfile();
        });

        this.loadingIndicator = new LoadingIndicator(this, "loading", (width - 30) / 2, 100, 30, 30);
        this.profileList = new ScrollablePanel(this, "defaultProfiles", 0, 40, width, height - 40);

        this.addToList(blankButton);
        this.addToList(duplicateButton);
        this.addToList(loadingIndicator);
        this.addToList(profileList);

        this.onlineProfileManager = new OnlineProfileManager();
        this.onlineProfileManager.getOnlineProfileNames(profiles -> {
            ConfigScreen screen = (ConfigScreen) this.getParent();
            this.loadingIndicator.setSelfVisible(false);

            for (String profile : profiles) {
                Button profileButton;
                this.profileList
                        .addToList(
                                profileButton = new Button(
                                        this.profileList, "p_" + profile, 0, 0, width, 30, new ColorHelper(-723724, -2039584, 0, -14671840), profile, FontUtils.HELVETICA_LIGHT_18
                                )
                        );
                profileButton.onClick((var3x, var4x) -> {
                    this.setLoading(true);
                    new Thread(() -> {
                        Profile onlineProfile = onlineProfileManager.downloadOnlineProfile(profile);
                        if (onlineProfile != null) {
                            screen.importProfile(onlineProfile);
                        }
                        this.setLoading(false);
                    }).start();
                });
            }

            this.profileList.getButton().accept(new GridLayoutVisitor(1));
        });
    }

    public void setLoading(boolean loading) {
        this.profileList.setSelfVisible(!loading);
        this.loadingIndicator.setSelfVisible(loading);
    }

    public void setExpanded(boolean expanded) {
        this.expandAnimation.changeDirection(!expanded ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = VecUtils.interpolate(this.expandAnimation.calcPercent(), 0.1, 0.81, 0.14, 1.0);
        if (this.expandAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            var4 = VecUtils.interpolate(this.expandAnimation.calcPercent(), 0.61, 0.01, 0.87, 0.16);
        }

        this.setHeight((int) ((float) this.maxHeight * var4));
        if (this.expandAnimation.calcPercent() != 0.0F) {
            RenderUtils.drawImage(
                    (float) this.x,
                    (float) (this.y + this.height),
                    (float) this.width,
                    50.0F,
                    Resources.shadowBottomPNG,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.expandAnimation.calcPercent() * partialTicks * 0.3F)
            );
            ScissorUtils.startScissor(this);
            RenderUtils.drawRoundedRect2(
                    (float) this.x, (float) this.y, (float) this.width, (float) this.height, ColorHelper.applyAlpha(-723724, partialTicks)
            );

            if (onlineProfileManager != null && onlineProfileManager.cachedProfileNames != null && onlineProfileManager.cachedProfileNames.isEmpty()) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_14,
                        (float) (this.x + 40),
                        (float) (this.y + 110),
                        "No Default Profiles Available",
                        ClientColors.MID_GREY.getColor()
                );
            }

            super.draw(partialTicks);
            ScissorUtils.restoreScissor();
        }
    }
}
