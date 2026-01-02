package io.github.sst.remake.module.impl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.bus.Priority;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;

public class CoordsModule extends Module {
    private final AnimationUtils coordinateAnimation = new AnimationUtils(1500, 1500, AnimationUtils.Direction.BACKWARDS);
    private double playerX, playerY, playerZ;

    public CoordsModule() {
        super(Category.GUI, "Coords", "Displays coordinates");
    }

    @Subscribe
    public void onPlayerTick(ClientPlayerTickEvent event) {
        boolean hasMoved = playerX != client.player.getX() || playerY != client.player.getY() || playerZ != client.player.getZ();
        playerX = client.player.getX();
        playerY = client.player.getY();
        playerZ = client.player.getZ();

        boolean shouldAnimate = hasMoved || (!client.player.isOnGround()) || client.player.isSneaking();
        if (!shouldAnimate) {
            if (this.coordinateAnimation.calcPercent() == 1.0F && this.coordinateAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
                this.coordinateAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            }
        } else {
            this.coordinateAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        }
    }

    @Subscribe(priority = Priority.LOWEST)
    public void onRender2D(RenderClient2DEvent event) {
        if (client.player == null || client.options.debugEnabled || client.options.hudHidden) return;
        float animationScale = Math.min(1.0F, 0.6F + this.coordinateAnimation.calcPercent() * 2.0F);
        String coordinatesText = String.format("%.0f %.0f %.0f", client.player.getX(), client.player.getY(), client.player.getZ());

        float textX = 85;
        int textY = event.getOffset();
        float maxTextWidth = 150;
        float textWidth = (float) FontUtils.HELVETICA_LIGHT_18.getWidth(coordinatesText);
        float scaleFactor = Math.min(1.0F, maxTextWidth / textWidth);

        if (this.coordinateAnimation.getDirection() != AnimationUtils.Direction.FORWARDS) {
            scaleFactor *= 0.9F + QuadraticEasing.easeInQuad(Math.min(1.0F, this.coordinateAnimation.calcPercent() * 8.0F), 0.0F, 1.0F, 1.0F) * 0.1F;
        } else {
            scaleFactor *= 0.9F + EasingFunctions.easeOutBack(Math.min(1.0F, this.coordinateAnimation.calcPercent() * 7.0F), 0.0F, 1.0F, 1.0F) * 0.1F;
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef(textX, (float) (textY + 10), 0.0F);
        RenderSystem.scalef(scaleFactor, scaleFactor, 1.0F);
        RenderSystem.translatef(-textX, (float) (-textY - 10), 0.0F);

        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18_BASIC, textX, textY, coordinatesText,
                ColorHelper.applyAlpha(-16777216, 0.5F * animationScale),
                FontAlignment.CENTER, FontAlignment.LEFT);

        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18, textX, textY, coordinatesText,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F * animationScale),
                FontAlignment.CENTER, FontAlignment.LEFT);

        RenderSystem.popMatrix();
    }

}
