package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class WaypointList extends ScrollablePanel {
    private static final int TRASHCAN_PADDING = 10;
    private static final int TRASHCAN_HITBOX_SIZE = 40;
    private static final int TRASHCAN_ICON_WIDTH = 22;
    private static final int TRASHCAN_ICON_HEIGHT = 26;
    private static final int TRASHCAN_ICON_OFFSET_X = 18;
    private static final int TRASHCAN_ICON_OFFSET_Y = 46;
    private static final int ROW_TOP_PADDING = 5;

    private static final int WAYPOINT_HEIGHT = 70;

    private final List<WaypointRow> waypointRows = new ArrayList<>();

    public AnimationUtils trashcanAnimation = new AnimationUtils(300, 300);
    public boolean isMouseOverTrashcan;
    public WaypointRow pendingTrashcanDeleteRow;

    public WaypointList(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height);
        this.trashcanAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.allowBottomOverflow = true;
        this.setListening(false);
    }

    public void addWaypoint(String displayName, Vec3i position, int color) {
        String waypointRowId = buildWaypointRowId(position);
        if (this.content.getChildByName(waypointRowId) != null) {
            return;
        }

        int rowY = this.getChildren().get(0).getChildren().size() * WAYPOINT_HEIGHT;
        WaypointRow waypointRow = new WaypointRow(
                this,
                waypointRowId,
                this.x,
                rowY,
                this.width,
                WAYPOINT_HEIGHT,
                displayName,
                position,
                color
        );

        waypointRow.targetY = waypointRow.getY();
        this.waypointRows.add(waypointRow);
        this.addToList(waypointRow);

        waypointRow.onClick((rowParent, mouseButton) -> {
            MapPanel mapPanel = (MapPanel) this.getParent();
            mapPanel.worldMapView.centerOn(waypointRow.position.getX(), waypointRow.position.getZ());
        });

        waypointRow.onPress(interactiveWidget -> removeWaypointRow(waypointRow));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        this.waypointRows.sort((a, b) -> a.targetY < b.targetY + a.getHeight() / 2 ? -1 : 1);

        if (shouldTriggerTrashcanDelete()) {
            this.pendingTrashcanDeleteRow.startDeleteAnimation();
            this.pendingTrashcanDeleteRow = null;
            this.isMouseOverTrashcan = false;
        }

        layoutWaypointRows();
        updateTrashcanState(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        animateNonDraggingRows();

        super.draw(partialTicks);

        int trashcanOffset = Math.round(
                QuadraticEasing.easeInQuad(
                        1.0F - this.trashcanAnimation.calcPercent(),
                        0.0F,
                        1.0F,
                        1.0F
                ) * 30.0F
        );

        RenderUtils.drawImage(
                (float) (this.x - trashcanOffset + TRASHCAN_ICON_OFFSET_X),
                (float) (this.height - TRASHCAN_ICON_OFFSET_Y),
                (float) TRASHCAN_ICON_WIDTH,
                (float) TRASHCAN_ICON_HEIGHT,
                Resources.TRASHCAN,
                ColorHelper.applyAlpha(
                        !this.isMouseOverTrashcan
                                ? ClientColors.DEEP_TEAL.getColor()
                                : ClientColors.PALE_YELLOW.getColor(),
                        this.trashcanAnimation.calcPercent() * 0.5F
                ),
                false
        );
    }

    private static String buildWaypointRowId(Vec3i position) {
        return "waypoint x" + position.getX() + " z" + position.getZ();
    }

    private void removeWaypointRow(WaypointRow row) {
        Client.INSTANCE.waypointTracker.getWaypoints().remove(
                new io.github.sst.remake.util.client.waypoint.Waypoint(
                        row.name,
                        row.position.getX(),
                        row.position.getZ(),
                        row.color
                )
        );

        Client.INSTANCE.waypointTracker.save();
        this.content.removeChildByName(row.name);
        this.waypointRows.remove(row);
    }

    private boolean shouldTriggerTrashcanDelete() {
        return this.pendingTrashcanDeleteRow != null
                && !this.pendingTrashcanDeleteRow.isDragging()
                && this.isMouseOverTrashcan;
    }

    private void layoutWaypointRows() {
        int currentY = 0;

        for (WaypointRow row : this.waypointRows) {
            if (!row.isDragging() && row.deleteSlideAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
                row.targetY = currentY + ROW_TOP_PADDING;
            } else {
                row.targetY = row.getY();
            }
            currentY += row.getHeight();
        }
    }

    private void updateTrashcanState(int mouseX, int mouseY) {
        boolean draggingAnyRow = false;

        for (WaypointRow row : this.waypointRows) {
            if (!row.isDragging()) {
                continue;
            }

            draggingAnyRow = true;
            this.trashcanAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);

            if (isMouseOverTrashcanArea(mouseX, mouseY)) {
                this.isMouseOverTrashcan = true;
                this.pendingTrashcanDeleteRow = row;
            } else {
                this.isMouseOverTrashcan = false;
                this.pendingTrashcanDeleteRow = null;
            }
            break;
        }

        if (!draggingAnyRow) {
            this.trashcanAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        }
    }

    private void animateNonDraggingRows() {
        float animationSpeed = Math.min(1.0F, 0.21F * (60.0F / (float) MinecraftClient.currentFps));

        for (WaypointRow row : this.waypointRows) {
            if (row.isDragging()) {
                continue;
            }

            float deltaY = (float) (row.getY() - row.targetY) * animationSpeed;
            if (Math.round(deltaY) == 0 && deltaY > 0.0F) {
                deltaY = 1.0F;
            } else if (Math.round(deltaY) == 0 && deltaY < 0.0F) {
                deltaY = -1.0F;
            }

            row.setY(Math.round((float) row.getY() - deltaY));
        }
    }

    private boolean isMouseOverTrashcanArea(int mouseX, int mouseY) {
        int left = this.getAbsoluteX() + TRASHCAN_PADDING;
        int right = this.getAbsoluteX() + TRASHCAN_PADDING + TRASHCAN_HITBOX_SIZE;

        int bottom = this.getAbsoluteY() + this.getHeight() - TRASHCAN_PADDING;
        int top = bottom - TRASHCAN_HITBOX_SIZE;

        return mouseX > left && mouseX < right && mouseY < bottom && mouseY > top;
    }
}