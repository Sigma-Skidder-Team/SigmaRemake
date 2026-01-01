package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointList extends ScrollableContentPanel {
    private List<Waypoint> field21209 = new ArrayList<>();
    public final int field21210 = 70;
    public AnimationUtils field21211 = new AnimationUtils(300, 300);
    public boolean field21212;
    public Waypoint field21213;

    public WaypointList(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.field21211.changeDirection(AnimationUtils.Direction.BACKWARDS);
        this.field20883 = true;
        this.setListening(false);
        this.method13511();
    }

    public void method13511() {
        boolean var3 = false;
    }

    public void addWaypoint(String var1, Vec3i var2, int var3) {
        String var6 = "waypoint x" + var2.getX() + " z" + var2.getZ();
        if (this.buttonList.getChildByName(var6) == null) {
            Waypoint waypoint = new Waypoint(
                    this, var6, this.x, this.getChildren().get(0).getChildren().size() * this.field21210, this.width, this.field21210, var1, var2, var3
            );
            waypoint.field21288 = waypoint.getY();
            this.field21209.add(waypoint);
            this.addToList(waypoint);
            waypoint.onClick((var2x, var3x) -> {
                MapPanel var6x = (MapPanel) this.getParent();
                var6x.field20614.method13077(waypoint.waypointPos.getX(), waypoint.waypointPos.getZ());
            });
            waypoint.onPress(
                    var3x -> {Client.INSTANCE.waypointManager.getWaypoints().remove(new io.github.sst.remake.util.client.waypoint.Waypoint(waypoint.waypointName, waypoint.waypointPos.getX(), waypoint.waypointPos.getZ(), waypoint.waypointColor));
                        this.buttonList.removeChildByName(waypoint.name);
                        this.field21209.remove(var3x);
                    }
            );
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.field21209.sort((var0, var1x) -> var0.field21288 < var1x.field21288 + var0.getHeight() / 2 ? -1 : 1);
        int var5 = 0;
        if (this.field21213 != null && !this.field21213.isDragging() && this.field21212) {
            this.field21213.method13608();
            this.field21213 = null;
            this.field21212 = false;
        }

        for (Waypoint var7 : this.field21209) {
            if (!var7.isDragging() && var7.field21290.getDirection() == AnimationUtils.Direction.BACKWARDS) {
                var7.field21288 = var5 + 5;
            } else {
                var7.field21288 = var7.getY();
            }

            var5 += var7.getHeight();
        }

        for (Waypoint var11 : this.field21209) {
            if (var11.isDragging()) {
                this.field21211.changeDirection(AnimationUtils.Direction.FORWARDS);
                if (mouseX > this.getAbsoluteX() + 10
                        && mouseX < this.getAbsoluteX() + 50
                        && mouseY < this.getAbsoluteY() + this.getHeight() - 10
                        && mouseY > this.getAbsoluteY() + this.getHeight() - 50) {
                    this.field21212 = true;
                    this.field21213 = var11;
                } else {
                    this.field21212 = false;
                    this.field21213 = null;
                }
                break;
            }

            if (!var11.isDragging() && this.field21211.getDirection() == AnimationUtils.Direction.FORWARDS) {
                Client.INSTANCE.waypointManager.getWaypoints().clear();

                for (Waypoint var9 : this.field21209) {
                    Client.INSTANCE.waypointManager.getWaypoints().add(new io.github.sst.remake.util.client.waypoint.Waypoint(var9.waypointName, var9.waypointPos.getX(), var9.waypointPos.getZ(), var9.waypointColor));
                }

                Collections.reverse(Client.INSTANCE.waypointManager.getWaypoints());
                Client.INSTANCE.waypointManager.save();
            }

            this.field21211.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = Math.min(1.0F, 0.21F * (60.0F / (float) MinecraftClient.currentFps));

        for (Waypoint var6 : this.field21209) {
            if (!var6.isDragging()) {
                float var7 = (float) (var6.getY() - var6.field21288) * var4;
                if (Math.round(var7) == 0 && var7 > 0.0F) {
                    var7 = 1.0F;
                } else if (Math.round(var7) == 0 && var7 < 0.0F) {
                    var7 = -1.0F;
                }

                var6.setY(Math.round((float) var6.getY() - var7));
            }
        }

        super.draw(partialTicks);
        int var8 = Math.round(QuadraticEasing.easeInQuad(1.0F - this.field21211.calcPercent(), 0.0F, 1.0F, 1.0F) * 30.0F);
        RenderUtils.drawImage(
                (float) (this.x - var8 + 18),
                (float) (this.height - 46),
                22.0F,
                26.0F,
                Resources.trashcanPNG,
                ColorHelper.applyAlpha(!this.field21212 ? ClientColors.DEEP_TEAL.getColor() : ClientColors.PALE_YELLOW.getColor(), this.field21211.calcPercent() * 0.5F),
                false
        );
    }
}