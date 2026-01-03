package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.GuiComponent;
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
import java.util.List;

public class WaypointList extends ScrollableContentPanel {
    private final List<Waypoint> waypoints = new ArrayList<>();
    public static final int WAYPOINT_HEIGHT = 70;
    public AnimationUtils trashcanAnimation = new AnimationUtils(300, 300);
    public boolean isMouseOverTrashcan;
    public Waypoint deletableWaypoint;

    public WaypointList(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.trashcanAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.field20883 = true;
        this.setListening(false);
    }

    public void addWaypoint(String name, Vec3i pos, int color) {
        String waypointId = "waypoint x" + pos.getX() + " z" + pos.getZ();
        if (this.buttonList.getChildByName(waypointId) == null) {
            Waypoint waypoint = new Waypoint(
                    this, waypointId, this.x, this.getChildren().get(0).getChildren().size() * WAYPOINT_HEIGHT, this.width, WAYPOINT_HEIGHT, name, pos, color
            );
            waypoint.targetY = waypoint.getY();
            this.waypoints.add(waypoint);
            this.addToList(waypoint);
            waypoint.onClick((var2x, var3x) -> {
                MapPanel mapPanel = (MapPanel) this.getParent();
                mapPanel.mapFrame.centerOn(waypoint.waypointPos.getX(), waypoint.waypointPos.getZ());
            });
            waypoint.onPress(
                    var3x -> {
                        Client.INSTANCE.waypointManager.getWaypoints().remove(new io.github.sst.remake.util.client.waypoint.Waypoint(waypoint.waypointName, waypoint.waypointPos.getX(), waypoint.waypointPos.getZ(), waypoint.waypointColor));
                        Client.INSTANCE.waypointManager.save();
                        this.buttonList.removeChildByName(waypoint.name);
                        this.waypoints.remove(var3x);
                    }
            );
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.waypoints.sort((w1, w2) -> w1.targetY < w2.targetY + w1.getHeight() / 2 ? -1 : 1);
        int currentY = 0;
        if (this.deletableWaypoint != null && !this.deletableWaypoint.isDragging() && this.isMouseOverTrashcan) {
            this.deletableWaypoint.startDeleteAnimation();
            this.deletableWaypoint = null;
            this.isMouseOverTrashcan = false;
        }

        for (Waypoint waypoint : this.waypoints) {
            if (!waypoint.isDragging() && waypoint.deleteAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
                waypoint.targetY = currentY + 5;
            } else {
                waypoint.targetY = waypoint.getY();
            }
            currentY += waypoint.getHeight();
        }

        for (Waypoint waypoint : this.waypoints) {
            if (waypoint.isDragging()) {
                this.trashcanAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
                if (mouseX > this.getAbsoluteX() + 10
                        && mouseX < this.getAbsoluteX() + 50
                        && mouseY < this.getAbsoluteY() + this.getHeight() - 10
                        && mouseY > this.getAbsoluteY() + this.getHeight() - 50) {
                    this.isMouseOverTrashcan = true;
                    this.deletableWaypoint = waypoint;
                } else {
                    this.isMouseOverTrashcan = false;
                    this.deletableWaypoint = null;
                }
                break;
            }

            this.trashcanAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        }
    }

    @Override
    public void draw(float partialTicks) {
        float animationSpeed = Math.min(1.0F, 0.21F * (60.0F / (float) MinecraftClient.currentFps));

        for (Waypoint waypoint : this.waypoints) {
            if (!waypoint.isDragging()) {
                float deltaY = (float) (waypoint.getY() - waypoint.targetY) * animationSpeed;
                if (Math.round(deltaY) == 0 && deltaY > 0.0F) {
                    deltaY = 1.0F;
                } else if (Math.round(deltaY) == 0 && deltaY < 0.0F) {
                    deltaY = -1.0F;
                }

                waypoint.setY(Math.round((float) waypoint.getY() - deltaY));
            }
        }

        super.draw(partialTicks);
        int trashcanOffset = Math.round(QuadraticEasing.easeInQuad(1.0F - this.trashcanAnimation.calcPercent(), 0.0F, 1.0F, 1.0F) * 30.0F);
        RenderUtils.drawImage(
                (float) (this.x - trashcanOffset + 18),
                (float) (this.height - 46),
                22.0F,
                26.0F,
                Resources.trashcanPNG,
                ColorHelper.applyAlpha(!this.isMouseOverTrashcan ? ClientColors.DEEP_TEAL.getColor() : ClientColors.PALE_YELLOW.getColor(), this.trashcanAnimation.calcPercent() * 0.5F),
                false
        );
    }
}