package io.github.sst.remake.module.impl.gui;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassModule extends Module {

    private static final Map<String, String> directions = new HashMap<String, String>() {{
        put("0", "S");
        put("90", "W");
        put("180", "N");
        put("270", "E");
        put("45", "SW");
        put("135", "NW");
        put("225", "NE");
        put("315", "SE");
    }};

    public CompassModule() {
        super(Category.GUI, "Compass", "Fornite style directions");
    }

    @Subscribe
    public void onRender(RenderClient2DEvent ignoredEvent) {
        if (client.player != null && !client.options.hudHidden) {
            int halfMarkers = 5;
            int markerSpacing = 60;
            int debugOffset = !client.options.debugEnabled ? 0 : 60;

            List<Integer> angles = this.generateCompassAngles((int) wrapDegrees(client.player.yaw), halfMarkers);
            int baseAngle = angles.get(halfMarkers);
            if (baseAngle == 0 && wrapDegrees(client.player.yaw) > 345.0F) {
                baseAngle = 360;
            }

            float angleOffset = 7.0F + wrapDegrees(client.player.yaw) - (float) baseAngle;
            double scrollOffset = angleOffset / 15.0F * (float) markerSpacing;

            GL11.glAlphaFunc(519, 0.0F);
            RenderUtils.drawImage(
                    (float) (client.getWindow().getWidth() / 2) - (float) (halfMarkers * markerSpacing) * 1.5F,
                    -40.0F,
                    (float) (halfMarkers * markerSpacing * 2) * 1.5F,
                    (float) (220 + debugOffset),
                    Resources.SHADOW,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.25F)
            );

            int index = 0;
            for (int angle : angles) {
                index++;
                double t1 = Math.max(0.0, Math.min(((double) (index * markerSpacing) - scrollOffset) / (double) ((float) (markerSpacing * halfMarkers)), 1.0));
                double t2 = Math.max(0.0, Math.min(2.25 - ((double) (index * markerSpacing) - scrollOffset) / (double) ((float) (markerSpacing * halfMarkers)), 1.0));
                float alphaFactor = (float) Math.min(t1, t2);
                this.renderCompassMarker(client.getWindow().getWidth() / 2 + index * markerSpacing - (int) scrollOffset - (halfMarkers + 1) * markerSpacing - 2, 30 + debugOffset, markerSpacing, angle, alphaFactor * 0.8F);
            }
        }
    }

    private void renderCompassMarker(int x, int y, int width, int angle, float alpha) {
        String label = directions.getOrDefault(angle + "", angle + "");

        if (!label.matches(".*\\d+.*")) {
            if (label.length() != 1) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_25,
                        (float) (x + (width - FontUtils.HELVETICA_LIGHT_25.getWidth(label)) / 2),
                        (float) (y + 20),
                        label,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha)
                );
            } else {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_MEDIUM_40,
                        (float) (x + (width - FontUtils.HELVETICA_MEDIUM_40.getWidth(label)) / 2),
                        (float) (y + 10),
                        label,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha)
                );
            }
        } else {
            RenderUtils.drawRoundedRect(
                    (float) (x + width / 2 - 1),
                    (float) (y + 28),
                    (float) (x + width / 2 + 1),
                    (float) (y + 38),
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * 0.5F)
            );
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_18,
                    (float) (x + (width - FontUtils.HELVETICA_LIGHT_18.getWidth(label)) / 2),
                    (float) (y + 40),
                    label,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha)
            );
        }
    }

    private List<Integer> generateCompassAngles(int baseAngle, int halfMarkers) {
        int roundedBase = roundToNearest15(baseAngle);
        List<Integer> angles = new ArrayList<>();

        for (int i = roundedBase - 15 * halfMarkers; i < roundedBase; i += 15) {
            angles.add((int) wrapDegrees((float) i));
        }

        for (int i = roundedBase; i < roundedBase + 15 * (halfMarkers + 1); i += 15) {
            angles.add((int) wrapDegrees((float) i));
        }

        return angles;
    }

    private int roundToNearest15(int value) {
        return (value + 7) / 15 * 15;
    }

    private float wrapDegrees(float angle) {
        angle %= 360.0F;
        if (angle < 0.0F) {
            angle += 360.0F;
        }

        return angle;
    }
}
