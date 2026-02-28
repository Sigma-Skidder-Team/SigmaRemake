package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.util.client.waypoint.WaypointColors;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.lwjgl.opengl.GL11;

public class WaypointColorBadge extends Button {
    public final WaypointColors color;
    public boolean isSelected;
    public AnimationUtils selectionAnimation;

    public WaypointColorBadge(GuiComponent parent, String id, int x, int y, WaypointColors color) {
        super(parent, id, x, y, 18, 18);
        this.color = color;

        this.selectionAnimation = new AnimationUtils(250, 250);
        this.selectionAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.isSelected && partialTicks == 1.0F) {
            this.selectionAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }

        float animPercent = this.selectionAnimation.calcPercent();
        int pulse = (int) (EasingFunctions.easeInOutCustomBack(animPercent, 0.0F, 1.0F, 1.0F, 7.0F) * 3.0F);

        float centerX = (float) (this.x + this.width / 2);
        float centerY = (float) (this.y + this.height / 2);

        RenderUtils.drawCircle(
                centerX,
                centerY,
                25.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.025F * partialTicks * animPercent)
        );
        RenderUtils.drawCircle(
                centerX,
                centerY,
                23.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks * animPercent)
        );
        RenderUtils.drawCircle(
                centerX,
                centerY,
                (float) (18 + pulse),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * animPercent)
        );
        RenderUtils.drawCircle(
                centerX,
                centerY,
                (float) (18 - pulse),
                ColorHelper.applyAlpha(this.color.color, partialTicks)
        );

        GL11.glPushMatrix();
        super.drawChildren(partialTicks);
        GL11.glPopMatrix();
    }
}
