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
    public AnimationUtils pageAnimation = new AnimationUtils(380, 200, AnimationUtils.Direction.FORWARDS);
    public ScrollablePanel changelogScrollPanel;
    private static JsonArray cachedChangelogJson;

    public ChangelogPage(GuiComponent parent, String id, int x, int y, int width, int height) {
        super(parent, id, x, y, width, height);

        this.setListening(false);

        this.changelogScrollPanel = new ScrollablePanel(this, "scroll", 100, 200, width - 200, height - 200);
        this.changelogScrollPanel.setReserveScrollbarSpace(true);

        this.showAlert(this.changelogScrollPanel);

        new Thread(() -> this.populateEntries(this.fetchChangelogJson())).start();
    }

    public void populateEntries(JsonArray changelogJson) {
        if (changelogJson == null) {
            return;
        }

        this.getParent().addRunnable(() -> {
            int currentY = 75;

            try {
                for (int i = 0; i < changelogJson.size(); i++) {
                    JsonObject entryJson = changelogJson.get(i).getAsJsonObject();

                    if (entryJson.has("url")) {
                        Util.getOperatingSystem().open(entryJson.get("url").getAsString());
                    }

                    ChangelogEntry entryWidget = new ChangelogEntry(this.changelogScrollPanel, "changelog" + i, entryJson);
                    this.changelogScrollPanel.getContent().showAlert(entryWidget);

                    entryWidget.setY(currentY);
                    currentY += entryWidget.getHeight();
                }
            } catch (JsonParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        if (this.changelogScrollPanel == null) {
            return;
        }

        if (this.isHovered() && this.isSelfVisible()) {
            for (GuiComponent child : this.changelogScrollPanel.getContent().getChildren()) {
                ChangelogEntry entry = (ChangelogEntry) child;
                entry.animation2.changeDirection(AnimationUtils.Direction.BACKWARDS);

                if ((double) entry.animation2.calcPercent() < 0.5) {
                    break;
                }
            }
            return;
        }

        for (GuiComponent child : this.changelogScrollPanel.getContent().getChildren()) {
            ChangelogEntry entry = (ChangelogEntry) child;
            entry.animation2.changeDirection(AnimationUtils.Direction.FORWARDS);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.pageAnimation.changeDirection(!this.isHovered()
                ? AnimationUtils.Direction.FORWARDS
                : AnimationUtils.Direction.BACKWARDS
        );

        partialTicks *= this.pageAnimation.calcPercent();

        float fadeFactor = VecUtils.interpolate(this.pageAnimation.calcPercent(), 0.17f, 1.0f, 0.51f, 1.0f);

        if (this.pageAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            fadeFactor = 1.0f;
        }

        this.setTranslateY((int) (150.0f * (1.0f - fadeFactor)));
        this.applyTranslationTransforms();

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_36,
                100.0F,
                100.0F,
                "Changelog",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );

        String versionText = "You're currently using Sigma Remake " + Client.VERSION;

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                100.0f,
                150.0f,
                versionText,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6f * partialTicks)
        );

        super.draw(partialTicks);
    }

    public JsonArray fetchChangelogJson() {
        if (cachedChangelogJson != null) {
            return cachedChangelogJson;
        }

        try {
            HttpEntity entity = NetUtils.getHttpClient()
                    .execute(new HttpGet("https://jelloconnect.sigmaclient.cloud/changelog.php?v=" + Client.VERSION + "remake"))
                    .getEntity();

            if (entity == null) {
                return null;
            }

            try (InputStream content = entity.getContent()) {
                cachedChangelogJson = new JsonParser()
                        .parse(IOUtils.toString(content, StandardCharsets.UTF_8))
                        .getAsJsonArray();
                return cachedChangelogJson;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get changelog", e);
        }
    }
}
