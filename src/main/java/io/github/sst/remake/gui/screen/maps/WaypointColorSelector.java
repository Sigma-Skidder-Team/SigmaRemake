package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.client.waypoint.WaypointColors;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ColorHelper;

public class WaypointColorSelector extends InteractiveWidget {
    public int selectedColor;

    public WaypointColorSelector(GuiComponent parent, String id, int x, int y) {
        super(parent, id, x, y, 200, 18, ColorHelper.DEFAULT_COLOR, false);

        int xOffset = 0;
        boolean selectFirst = true;

        for (WaypointColors waypointColor : WaypointColors.values()) {
            String badgeId = "badge" + waypointColor.name;

            xOffset += 25;

            WaypointColorBadge badge = new WaypointColorBadge(this, badgeId, xOffset, 0, waypointColor);
            this.addToList(badge);

            if (selectFirst) {
                badge.isSelected = true;
                this.selectedColor = waypointColor.color;
                selectFirst = false;
            }

            badge.onClick((clickedBadge, mouseButton) -> {
                for (GuiComponent child : clickedBadge.getParent().getChildren()) {
                    if (child instanceof WaypointColorBadge) {
                        WaypointColorBadge otherBadge = (WaypointColorBadge) child;
                        otherBadge.isSelected = false;
                        otherBadge.selectionAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
                    }
                }

                WaypointColorBadge selectedBadge = (WaypointColorBadge) clickedBadge;
                selectedBadge.isSelected = true;
                selectedBadge.selectionAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);

                this.selectedColor = selectedBadge.color.color;
            });
        }
    }
}
