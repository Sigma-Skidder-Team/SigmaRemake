package io.github.sst.remake.gui.screen.mainmenu;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.http.NetUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ChangelogPage extends GuiComponent {
    public AnimationUtils animation = new AnimationUtils(380, 200, AnimationUtils.Direction.FORWARDS);
    public ScrollablePanel scrollPanel;
    private static JsonArray cachedChangelog;

    public ChangelogPage(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        this.scrollPanel = new ScrollablePanel(this, "scroll", 100, 200, var5 - 200, var6 - 200);
        this.scrollPanel.setReserveScrollbarSpace(true);
        this.showAlert(this.scrollPanel);
        new Thread(() -> this.method13490(this.getChangelog())).start();
    }

    public void method13490(JsonArray changelogJson) {
        if (changelogJson != null) {
            this.getParent().addRunnable(() -> {
                int y = 75;

                try {
                    for (int i = 0; i < changelogJson.size(); i++) {
                        JsonObject entry = changelogJson.get(i).getAsJsonObject();
                        ChangelogEntry changelogEntry;
                        if (entry.has("url")) {
                            Util.getOperatingSystem().open(entry.get("url").getAsString());
                        }

                        this.scrollPanel.getContent().showAlert(changelogEntry = new ChangelogEntry(this.scrollPanel, "changelog" + i, entry));
                        changelogEntry.setY(y);
                        y += changelogEntry.getHeight();
                    }
                } catch (JsonParseException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.scrollPanel != null) {
            if (this.isHovered() && this.isSelfVisible()) {
                for (GuiComponent var9 : this.scrollPanel.getContent().getChildren()) {
                    ChangelogEntry var10 = (ChangelogEntry) var9;
                    var10.animation2.changeDirection(AnimationUtils.Direction.BACKWARDS);
                    if ((double) var10.animation2.calcPercent() < 0.5) {
                        break;
                    }
                }
            } else {
                for (GuiComponent var6 : this.scrollPanel.getContent().getChildren()) {
                    ChangelogEntry var7 = (ChangelogEntry) var6;
                    var7.animation2.changeDirection(AnimationUtils.Direction.FORWARDS);
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.animation.changeDirection(!this.isHovered() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        partialTicks *= this.animation.calcPercent();

        float fadeFactor = VecUtils.interpolate(this.animation.calcPercent(), 0.17f, 1.0f, 0.51f, 1.0f);

        if (this.animation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            fadeFactor = 1.0f;
        }

        this.setTranslateY((int) (150.0f * (1.0f - fadeFactor)));
        this.applyTranslationTransforms();
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
                HttpEntity entity = NetUtils.getHttpClient().execute(new HttpGet("https://jelloconnect.sigmaclient.cloud/changelog.php?v=1.0.0remake")).getEntity();
                if (entity != null) {
                    try (InputStream content = entity.getContent()) {
                        return cachedChangelog = new JsonParser().parse(IOUtils.toString(content, StandardCharsets.UTF_8)).getAsJsonArray();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get changelog", e);
            }
        }

        return cachedChangelog;
    }

}
