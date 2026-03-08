package io.github.sst.remake.manager.impl;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class NotificationManager extends Manager implements IMinecraft {
    private static final int ANIMATION_TIME = 200;
    private List<Notification> notifications;

    @Override
    public void init() {
        notifications = new ArrayList<>();
        super.init();
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        this.notifications.removeIf(notification -> notification.timer.getElapsedTime() > (long) notification.showTime);
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (client.options.hudHidden) return;
        
        for (int index = 0; index < this.notifications.size(); index++) {
            Notification notification = this.notifications.get(index);

            float animation = this.getAnimation(notification);

            int notificationWidth = 340;
            int marginRight = 10;

            int x = client.getWindow().getWidth()
                    - marginRight
                    - (int) (notificationWidth * animation * animation);

            int notificationHeight = 64;
            int marginBottom = 10;
            int padding = 10;

            int y = client.getWindow().getHeight()
                    - notificationHeight
                    - marginBottom
                    - index * (int) (
                    notificationHeight * this.getStackAnimationProgress(index)
                            + padding * this.getStackAnimationProgress(index)
            );

            float alpha = Math.min(1.0F, animation);

            int backgroundColor = new Color(0.14F, 0.14F, 0.14F, alpha * 0.93F).getRGB();

            int borderColor = new Color(
                    0.0F,
                    0.0F,
                    0.0F,
                    Math.min(animation * 0.075F, 1.0F)
            ).getRGB();

            int textColor = new Color(1.0F, 1.0F, 1.0F, alpha).getRGB();

            RenderUtils.drawRoundedRect(
                    (float) x,
                    (float) y,
                    (float) notificationWidth,
                    (float) notificationHeight,
                    10.0F,
                    alpha
            );

            RenderUtils.drawRoundedRect(
                    (float) x,
                    (float) y,
                    (float) (x + notificationWidth),
                    (float) (y + notificationHeight),
                    backgroundColor
            );

            RenderUtils.drawRoundedRect(
                    (float) x,
                    (float) y,
                    (float) (x + notificationWidth),
                    (float) (y + 1),
                    borderColor
            );

            RenderUtils.drawRoundedRect(
                    (float) x,
                    (float) (y + notificationHeight - 1),
                    (float) (x + notificationWidth),
                    (float) (y + notificationHeight),
                    borderColor
            );

            RenderUtils.drawRoundedRect(
                    (float) x,
                    (float) (y + 1),
                    (float) (x + 1),
                    (float) (y + notificationHeight - 1),
                    borderColor
            );

            RenderUtils.drawRoundedRect(
                    (float) (x + notificationWidth - 1),
                    (float) (y + 1),
                    (float) (x + notificationWidth),
                    (float) (y + notificationHeight - 1),
                    borderColor
            );

            ScissorUtils.startScissorNoGL(
                    x,
                    y,
                    x + notificationWidth - padding,
                    y + notificationHeight
            );

            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_20,
                    (float) (x + notificationHeight + padding - 2),
                    (float) (y + padding),
                    notification.title,
                    textColor
            );

            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) (x + notificationHeight + padding - 2),
                    (float) (
                            y + padding
                                    + FontUtils.HELVETICA_LIGHT_20.getHeight(notification.title)
                    ),
                    notification.desc,
                    textColor
            );

            ScissorUtils.restoreScissor();

            RenderUtils.drawImage(
                    (float) (x + padding / 2),
                    (float) (y + padding / 2),
                    (float) (notificationHeight - padding),
                    (float) (notificationHeight - padding),
                    notification.icon
            );
        }
    }

    public void send(Notification notification) {
        for (Notification existing : this.notifications) {
            if (existing.equals(notification)) {

                existing.timer.setElapsedTime(
                        Math.min(existing.timer.getElapsedTime(), ANIMATION_TIME + 1)
                );

                existing.desc = notification.desc;
                existing.icon = notification.icon;

                return;
            }
        }

        this.notifications.add(notification);
    }

    public boolean isRendering() {
        for (Notification notification : this.notifications) {
            if (this.getAnimation(notification) > 0) {
                return true;
            }
        }

        return false;
    }

    public float getAnimation(Notification notification) {
        float elapsed = (float) Math.min(
                notification.timer.getElapsedTime(),
                notification.showTime
        );

        if (elapsed >= ANIMATION_TIME * 1.4F) {
            if (elapsed <= notification.showTime - ANIMATION_TIME) {
                return 1.0F;
            }

            return QuadraticEasing.easeInQuad(
                    ((float) notification.showTime - elapsed) / ANIMATION_TIME,
                    0.0F,
                    1.0F,
                    1.0F
            );
        }

        return QuadraticEasing.easeOutQuad(
                elapsed / (ANIMATION_TIME * 1.4F),
                0.0F,
                1.0F,
                1.0F
        );
    }

    public float getStackAnimationProgress(int index) {
        float total = 0.0F;

        for (int i = 0; i < index; i++) {
            total += this.getAnimation(this.notifications.get(i));
        }

        return total / (float) index;
    }
}