package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.math.Vec3i;

public class WaypointRow extends InteractiveWidget {
    public int targetY;
    public final AnimationUtils dragHighlightAnimation;
    public final AnimationUtils deleteSlideAnimation;
    public String name;
    public Vec3i position;
    public int color;

    public WaypointRow(GuiComponent parent, String id, int x, int y, int width, int height, String name, Vec3i position, int color) {
        super(parent, id, x, y, width, height, true);

        this.targetY = y;

        this.dragHighlightAnimation = new AnimationUtils(114, 114);

        this.deleteSlideAnimation = new AnimationUtils(200, 200);
        this.deleteSlideAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);

        this.name = name;
        this.position = position;
        this.color = color;

        this.allowBottomOverflow = true;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        this.dragHighlightAnimation.changeDirection(
                !this.isDragging() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS
        );

        boolean canDragNow = this.isDragging() || mouseX > this.getAbsoluteX() + this.getWidth() - 62;
        this.setDraggable(canDragNow);

        if (this.deleteSlideAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            this.setDraggable(false);

            this.setX(Math.round(
                    (float) this.getWidth() * QuadraticEasing.easeInQuad(this.deleteSlideAnimation.calcPercent(), 0.0F, 1.0F, 1.0F)
            ));

            if (this.deleteSlideAnimation.calcPercent() == 1.0F) {
                this.firePressHandlers();
            }
        }
    }

    public void startDeleteAnimation() {
        this.deleteSlideAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedRect2(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(
                        ColorHelper.shiftTowardsBlack(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.03F),
                        this.dragHighlightAnimation.calcPercent()
                )
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.x + 68),
                (float) (this.y + 14),
                this.name,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F)
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x + 68),
                (float) (this.y + 38),
                "x:" + this.position.getX() + " z:" + this.position.getZ(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F)
        );

        int dragHandleX = this.width - 43;
        float dragHandleAlpha = !this.isDragging() ? 0.2F : 0.4F;

        RenderUtils.drawRoundedRect2(
                (float) (this.x + dragHandleX),
                (float) (this.y + 27),
                20.0F,
                2.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), dragHandleAlpha)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + dragHandleX),
                (float) (this.y + 32),
                20.0F,
                2.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), dragHandleAlpha)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + dragHandleX),
                (float) (this.y + 37),
                20.0F,
                2.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), dragHandleAlpha)
        );

        RenderUtils.drawCircle(
                (float) (this.x + 35),
                (float) (this.y + this.height / 2),
                20.0F,
                ColorHelper.shiftTowardsOther(this.color, ClientColors.DEEP_TEAL.getColor(), 0.9F)
        );
        RenderUtils.drawCircle(
                (float) (this.x + 35),
                (float) (this.y + this.height / 2),
                17.0F,
                this.color
        );

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                14.0F,
                partialTicks * 0.2F * this.dragHighlightAnimation.calcPercent()
        );

        super.draw(partialTicks);
    }
}