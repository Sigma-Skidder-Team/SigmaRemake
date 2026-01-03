package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.util.client.waypoint.WaypointColors;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ColorHelper;

public class BadgeSelect extends InteractiveWidget {
    public int field21296;

    public BadgeSelect(CustomGuiScreen var1, String var2, int var3, int var4) {
        super(var1, var2, var3, var4, 200, 18, ColorHelper.DEFAULT_COLOR, false);
        int offset = 0;
        boolean var7 = true;

        for (WaypointColors var11 : WaypointColors.values()) {
            String var10004 = "badge" + var11.name;
            offset += 25;
            ActualWaypoint var12;
            this.addToList(var12 = new ActualWaypoint(this, var10004, offset, 0, var11));
            if (var7) {
                var12.field20598 = true;
                this.field21296 = var11.color;
            }

            var12.onClick((var1x, var2x) -> {
                for (CustomGuiScreen var6 : var1x.getParent().getChildren()) {
                    if (var6 instanceof ActualWaypoint) {
                        ((ActualWaypoint) var6).field20598 = false;
                        ((ActualWaypoint) var6).field20599.changeDirection(AnimationUtils.Direction.FORWARDS);
                    }
                }

                ((ActualWaypoint) var1x).field20598 = true;
                ((ActualWaypoint) var1x).field20599.changeDirection(AnimationUtils.Direction.BACKWARDS);
                this.field21296 = ((ActualWaypoint) var1x).color.color;
            });
            var7 = false;
        }
    }
}
