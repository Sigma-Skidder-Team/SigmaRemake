package io.github.sst.remake.gui.element.impl.cgui.config;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.impl.JelloScreen;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.client.Profile;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Element {
    public final AnimationUtils field21298;
    public ScrollableContentPanel profileScrollView;
    public ConfigGroup field21300;
    private final List<ProfileGroup> field21301 = new ArrayList<>();

    public ConfigScreen(CustomGuiScreen var1, String var2, int var3, int var4) {
        super(var1, var2, var3 - 250, var4 - 500, 250, 500, ColorHelper.DEFAULT_COLOR, false);
        this.field21298 = new AnimationUtils(300, 100);
        this.setReAddChildren(true);
        this.setListening(false);
        TextButton addButton;
        this.addToList(
                addButton = new TextButton(
                        this, "addButton", this.width - 55, 0, FontUtils.HELVETICA_LIGHT_25.getWidth("Add"), 69, ColorHelper.DEFAULT_COLOR, "+", FontUtils.HELVETICA_LIGHT_25
                )
        );
        addButton.onClick((var1x, var2x) -> this.field21300.method13119(true));
        this.addToList(this.field21300 = new ConfigGroup(this, "profile", 0, 69, this.width, 200));
        this.field21300.setReAddChildren(true);
        this.method13615();
    }

    public void method13610() {
        Profile profile = Client.INSTANCE.configManager.profile;

        if (profile == null) {
            return;
        }

        int var5 = 1;

        while (Client.INSTANCE.configManager.getByName(profile.name + " Copy " + var5)) {
            var5++;
        }

        Client.INSTANCE.configManager.saveProfile(profile.cloneWithName(profile.name + " Copy " + var5));
        this.addRunnable(() -> this.method13615());
        this.field21300.method13119(false);
    }

    public void method13611(Profile var1) {
        int profile = 1;

        while (Client.INSTANCE.configManager.getByName(var1.name + " " + profile)) {
            profile++;
        }

        Client.INSTANCE.configManager.saveProfile(var1.cloneWithName(var1.name + " " + profile));
        this.addRunnable(() -> this.method13615());
        this.field21300.method13119(false);
    }

    public void method13612() {
        int profile = 1;

        while (Client.INSTANCE.configManager.getByName("New Profile " + profile)) {
            profile++;
        }

        Client.INSTANCE.configManager.saveProfile(new Profile("New Profile " + profile, new JsonObject()));
        this.addRunnable(this::method13615);
        this.field21300.method13119(false);
    }

    public void method13613() {
        this.field21300.field20703.changeDirection(AnimationUtils.Direction.BACKWARDS);
        if (this.field21298.getDirection() != AnimationUtils.Direction.BACKWARDS) {
            this.field21298.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }
    }

    public boolean method13614() {
        return this.field21298.getDirection() == AnimationUtils.Direction.BACKWARDS && this.field21298.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (mouseY > this.field21300.method13272() + this.field21300.getHeight()) {
            this.field21300.method13119(false);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void method13615() {
        int var3 = 0;
        if (this.profileScrollView != null) {
            var3 = this.profileScrollView.getScrollOffset();
            this.removeChildren(this.profileScrollView);
        }

        this.addToList(this.profileScrollView = new ScrollableContentPanel(this, "profileScrollView", 10, 80, this.width - 20, this.height - 80 - 10));
        this.profileScrollView.setScrollOffset(var3);
        this.field21301.clear();
        int var4 = 0;
        int var5 = 70;

        for (Profile var7 : Client.INSTANCE.configManager.profiles) {
            ProfileGroup var8 = new ProfileGroup(this, "profile" + var4, 0, var5 * var4, this.profileScrollView.getWidth(), var5, var7, var4);
            this.profileScrollView.addToList(var8);
            this.field21301.add(var8);
            var4++;
        }

        JelloScreen var9 = (JelloScreen) this.getParent();
        var9.method13315();
    }

    public void method13616() {
        int var3 = 0;

        for (ProfileGroup var5 : this.field21301) {
            var5.setY(var3);
            var3 += var5.getHeight();
        }
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.field21298.calcPercent();
        this.method13616();
        float var4 = VecUtils.interpolate(partialTicks, 0.37, 1.48, 0.17, 0.99);
        if (this.field21298.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var4 = VecUtils.interpolate(partialTicks, 0.38, 0.73, 0.0, 1.0);
        }

        this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        this.drawBackground((int) ((float) this.width * 0.25F * (1.0F - var4)));
        this.method13284((int) ((float) this.width * 0.14F * (1.0F - var4)));
        super.method13224();
        super.method13225();
        int var5 = 10;
        int var6 = ColorHelper.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtils.drawRoundedRect(
                (float) (this.x + var5 / 2),
                (float) (this.y + var5 / 2),
                (float) (this.width - var5),
                (float) (this.height - var5),
                35.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + var5 / 2),
                (float) (this.y + var5 / 2),
                (float) (this.x - var5 / 2 + this.width),
                (float) (this.y - var5 / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtils.drawRoundedRect((float) this.x, (float) this.y, (float) this.width, (float) this.height, (float) var5, var6);
        float var7 = 0.9F + (1.0F - VecUtils.interpolate(this.field21300.field20703.calcPercent(), 0.0, 0.96, 0.69, 0.99)) * 0.1F;
        if (this.field21300.field20703.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var7 = 0.9F + (1.0F - VecUtils.interpolate(this.field21300.field20703.calcPercent(), 0.61, 0.01, 0.87, 0.16)) * 0.1F;
        }

        this.profileScrollView.method13279(var7, var7);
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
