package io.github.sst.remake.gui.impl.menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.changelog.ChangelogLoader;
import io.github.sst.remake.gui.element.impl.changelog.Change;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.newdawn.slick.TrueTypeFont;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ChangelogPage extends CustomGuiScreen {
    public AnimationUtils animation = new AnimationUtils(380, 200, AnimationUtils.Direction.BACKWARDS);
    public ScrollableContentPanel scrollPanel;
    private static JsonArray cachedChangelog;

    public ChangelogPage(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        this.scrollPanel = new ScrollableContentPanel(this, "scroll", 100, 200, var5 - 200, var6 - 200);
        this.scrollPanel.method13518(true);
        this.showAlert(this.scrollPanel);
        new Thread(() -> this.method13490(this.getChangelog())).start();
    }

    public void method13490(JsonArray var1) {
        if (var1 != null) {
            this.getParent().addRunnable(new ChangelogLoader(this, var1));
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.scrollPanel != null) {
            if (this.isHovered() && this.isSelfVisible()) {
                for (CustomGuiScreen var9 : this.scrollPanel.getButton().getChildren()) {
                    Change var10 = (Change) var9;
                    var10.animation2.changeDirection(AnimationUtils.Direction.FORWARDS);
                    if ((double) var10.animation2.calcPercent() < 0.5) {
                        break;
                    }
                }
            } else {
                for (CustomGuiScreen var6 : this.scrollPanel.getButton().getChildren()) {
                    Change var7 = (Change) var6;
                    var7.animation2.changeDirection(AnimationUtils.Direction.BACKWARDS);
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.animation.changeDirection(!this.isHovered() ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        partialTicks *= this.animation.calcPercent();

        float fadeFactor = VecUtils.interpolate(this.animation.calcPercent(), 0.17f, 1.0f, 0.51f, 1.0f);

        if (this.animation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            fadeFactor = 1.0f;
        }

        this.drawBackground((int) (150.0f * (1.0f - fadeFactor)));
        this.method13225();
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_36, 100.0F, 100.0F, "Changelog", ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks));
        TrueTypeFont jelloLightFont25 = FontUtils.HELVETICA_LIGHT_25;
        String versionText = "You're currently using Sigma Remake " + Client.VERSION;
        RenderUtils.drawString(
                jelloLightFont25,
                100.0f, 150.0f,
                versionText,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6f * partialTicks)
        );
        super.draw(partialTicks);
    }

    public JsonArray getChangelog() {
        if (cachedChangelog == null) {
            try {
                HttpEntity entity = HttpClients.createDefault().execute(new HttpGet("https://jelloconnect.sigmaclient.cloud/changelog.php?v=1.0.0remake")).getEntity();
                if (entity != null) {
                    try (InputStream content = entity.getContent()) {
                        return cachedChangelog = JsonParser.parseString(IOUtils.toString(content, StandardCharsets.UTF_8)).getAsJsonArray();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get changelog", e);
            }
        }

        return cachedChangelog;
    }

}
