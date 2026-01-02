package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.math.Vec3i;

public class Waypoint extends Element {
    public int targetY;
    public final AnimationUtils dragAnimation;
    public final AnimationUtils deleteAnimation;
    public String waypointName;
    public Vec3i waypointPos;
    public int waypointColor;

    public Waypoint(CustomGuiScreen var1, String var2, int x, int y, int width, int height, String var7, Vec3i var8, int var9) {
        super(var1, var2, x, y, width, height, true);
        this.targetY = y;
        this.dragAnimation = new AnimationUtils(114, 114);
        this.deleteAnimation = new AnimationUtils(200, 200);
        this.deleteAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        this.waypointName = var7;
        this.waypointPos = var8;
        this.waypointColor = var9;
        this.field20883 = true;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        this.dragAnimation.changeDirection(!this.isDragging() ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        boolean var5 = this.isDragging() || newHeight > this.getAbsoluteX() + this.getWidth() - 62;
        this.setDraggable(var5);
        if (this.deleteAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            this.setDraggable(false);
            this.setX(Math.round((float) this.getWidth() * QuadraticEasing.easeInQuad(this.deleteAnimation.calcPercent(), 0.0F, 1.0F, 1.0F)));
            if (this.deleteAnimation.calcPercent() == 1.0F) {
                this.callUIHandlers();
            }
        }
    }

    public void startDeleteAnimation() {
        this.deleteAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedRect2(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsBlack(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.03F), this.dragAnimation.calcPercent())
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.x + 68),
                (float) (this.y + 14),
                this.waypointName,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x + 68),
                (float) (this.y + 38),
                "x:" + this.waypointPos.getX() + " z:" + this.waypointPos.getZ(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F)
        );
        int var5 = this.width - 43;
        float var6 = !this.isDragging() ? 0.2F : 0.4F;
        RenderUtils.drawRoundedRect2(
                (float) (this.x + var5), (float) (this.y + 27), 20.0F, 2.0F, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + var5), (float) (this.y + 27 + 5), 20.0F, 2.0F, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + var5), (float) (this.y + 27 + 10), 20.0F, 2.0F, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
        );
        RenderUtils.drawCircle(
                (float) (this.x + 35),
                (float) (this.y + this.height / 2),
                20.0F,
                ColorHelper.shiftTowardsOther(this.waypointColor, ClientColors.DEEP_TEAL.getColor(), 0.9F)
        );
        RenderUtils.drawCircle((float) (this.x + 35), (float) (this.y + this.height / 2), 17.0F, this.waypointColor);
        RenderUtils.drawRoundedRect(
                (float) this.x, (float) this.y, (float) this.width, (float) this.height, 14.0F, partialTicks * 0.2F * this.dragAnimation.calcPercent()
        );
        super.draw(partialTicks);
    }
}
