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
    public boolean field20598;
    public AnimationUtils field20599;

    public WaypointColorBadge(GuiComponent var1, String var2, int var3, int var4, WaypointColors color) {
        super(var1, var2, var3, var4, 18, 18);
        this.color = color;
        this.field20599 = new AnimationUtils(250, 250);
        this.field20599.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.field20598 && partialTicks == 1.0F) {
            this.field20599.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }

        int var4 = (int) (EasingFunctions.easeInOutCustomBack(this.field20599.calcPercent(), 0.0F, 1.0F, 1.0F, 7.0F) * 3.0F);
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                25.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.025F * partialTicks * this.field20599.calcPercent())
        );
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                23.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks * this.field20599.calcPercent())
        );
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                (float) (18 + var4),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.field20599.calcPercent())
        );
        RenderUtils.drawCircle(
                (float) (this.x + this.width / 2),
                (float) (this.y + this.height / 2),
                (float) (18 - var4),
                ColorHelper.applyAlpha(this.color.color, partialTicks)
        );
        GL11.glPushMatrix();
        super.drawChildren(partialTicks);
        GL11.glPopMatrix();
    }
}
