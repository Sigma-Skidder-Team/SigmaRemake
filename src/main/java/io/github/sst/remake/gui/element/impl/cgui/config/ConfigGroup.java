package io.github.sst.remake.gui.element.impl.cgui.config;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.element.impl.alert.LoadingIndicator;
import io.github.sst.remake.gui.element.impl.drop.GridLayoutVisitor;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.manager.impl.OnlineProfilesManager;
import io.github.sst.remake.profile.Profile;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ConfigGroup extends Element {
    public AnimationUtils field20703 = new AnimationUtils(300, 200, AnimationUtils.Direction.BACKWARDS);
    private final int field20704;
    private final ScrollableContentPanel profileList;
    private final OnlineProfilesManager onlineProfiles;
    private final LoadingIndicator loadingIndicator;

    public ConfigGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, 0, ColorHelper.DEFAULT_COLOR, false);
        this.field20704 = var6;

        TextButton blankButton = new TextButton(this, "blankButton", 25, 0, FontUtils.HELVETICA_LIGHT_20.getWidth("Blank"), 30, ColorHelper.DEFAULT_COLOR, "Blank", FontUtils.HELVETICA_LIGHT_20);

        blankButton.onClick((var1x, var2x) -> {
            ConfigScreen var5x = (ConfigScreen) this.getParent();
            var5x.method13612();
        });

        TextButton duplicateButton = new TextButton(this, "dupeButton", var5 - 25 - FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"), 0, FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"), 30, ColorHelper.DEFAULT_COLOR, "Duplicate", FontUtils.HELVETICA_LIGHT_20);

        duplicateButton.onClick((var1x, var2x) -> {
            ConfigScreen var5x = (ConfigScreen) this.getParent();
            var5x.method13610();
        });

        this.loadingIndicator = new LoadingIndicator(this, "loading", (var5 - 30) / 2, 100, 30, 30);
        this.profileList = new ScrollableContentPanel(this, "defaultProfiles", 0, 40, var5, var6 - 40);

        this.addToList(blankButton);
        this.addToList(duplicateButton);
        this.addToList(loadingIndicator);
        this.addToList(profileList);

        this.onlineProfiles = new OnlineProfilesManager();
        this.onlineProfiles.cache(profiles -> {
            ConfigScreen screen = (ConfigScreen) this.getParent();
            this.loadingIndicator.setSelfVisible(false);

            for (String profile : profiles) {
                Button profileButton;
                this.profileList
                        .addToList(
                                profileButton = new Button(
                                        this.profileList, "p_" + profile, 0, 0, var5, 30, new ColorHelper(-723724, -2039584, 0, -14671840), profile, FontUtils.HELVETICA_LIGHT_18
                                )
                        );
                profileButton.onClick((var3x, var4x) -> {
                    this.method13118(true);
                    new Thread(() -> {
                        Profile onlineProfile = onlineProfiles.createProfileFromOnlineConfig(Client.INSTANCE.configManager.currentProfile, profile);
                        screen.method13611(onlineProfile);
                        this.method13118(false);
                    }).start();
                });
            }

            this.profileList.getButton().accept(new GridLayoutVisitor(1));
        });
    }

    public void method13118(boolean var1) {
        this.profileList.setSelfVisible(!var1);
        this.loadingIndicator.setSelfVisible(var1);
    }

    public void method13119(boolean var1) {
        this.field20703.changeDirection(!var1 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = VecUtils.interpolate(this.field20703.calcPercent(), 0.1, 0.81, 0.14, 1.0);
        if (this.field20703.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var4 = VecUtils.interpolate(this.field20703.calcPercent(), 0.61, 0.01, 0.87, 0.16);
        }

        this.setHeight((int) ((float) this.field20704 * var4));
        if (this.field20703.calcPercent() != 0.0F) {
            RenderUtils.drawImage(
                    (float) this.x,
                    (float) (this.y + this.height),
                    (float) this.width,
                    50.0F,
                    Resources.shadowBottomPNG,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field20703.calcPercent() * partialTicks * 0.3F)
            );
            ScissorUtils.startScissor(this);
            RenderUtils.drawRoundedRect2(
                    (float) this.x, (float) this.y, (float) this.width, (float) this.height, ColorHelper.applyAlpha(-723724, partialTicks)
            );

            if (onlineProfiles != null && onlineProfiles.cachedOnlineProfiles != null && onlineProfiles.cachedOnlineProfiles.isEmpty()) {
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
