package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.client.waypoint.WaypointColors;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ColorHelper;

public class WaypointColorSelector extends InteractiveWidget {
    public int field21296;

    public WaypointColorSelector(GuiComponent var1, String var2, int var3, int var4) {
        super(var1, var2, var3, var4, 200, 18, ColorHelper.DEFAULT_COLOR, false);
        int offset = 0;
        boolean var7 = true;

        for (WaypointColors var11 : WaypointColors.values()) {
            String var10004 = "badge" + var11.name;
            offset += 25;
            WaypointColorBadge var12;
            this.addToList(var12 = new WaypointColorBadge(this, var10004, offset, 0, var11));
            if (var7) {
                var12.field20598 = true;
                this.field21296 = var11.color;
            }

            var12.onClick((parent, mouseButton) -> {
                for (GuiComponent var6 : parent.getParent().getChildren()) {
                    if (var6 instanceof WaypointColorBadge) {
                        ((WaypointColorBadge) var6).field20598 = false;
                        ((WaypointColorBadge) var6).field20599.changeDirection(AnimationUtils.Direction.FORWARDS);
                    }
                }

                ((WaypointColorBadge) parent).field20598 = true;
                ((WaypointColorBadge) parent).field20599.changeDirection(AnimationUtils.Direction.BACKWARDS);
                this.field21296 = ((WaypointColorBadge) parent).color.color;
            });
            var7 = false;
        }
    }
}
