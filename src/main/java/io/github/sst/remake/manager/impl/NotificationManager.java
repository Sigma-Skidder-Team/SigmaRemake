package io.github.sst.remake.manager.impl;

import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.player.ClientPlayerTickEvent;
import io.github.sst.remake.gui.element.impl.Notification;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager extends Manager implements IMinecraft {

    private final List<Notification> notifications = new ArrayList<>();
    private final int field39922 = 200;

    public void send(Notification notification) {
        for (Notification notif : this.notifications) {
            if (notif.equals(notification)) {
                notif.time.setElapsedTime(Math.min(notif.time.getElapsedTime(), this.field39922 + 1));
                notif.desc = notification.desc;
                notif.field43610++;
                notif.icon = notification.icon;
                return;
            }
        }

        this.notifications.add(notification);
    }

    public float getAnimation(Notification var1) {
        float var4 = (float) Math.min(var1.time.getElapsedTime(), var1.showTime);
        if (!(var4 < (float) this.field39922 * 1.4F)) {
            return !(var4 > (float) var1.showTime - (float) this.field39922)
                    ? 1.0F
                    : QuadraticEasing.easeInQuad(((float) var1.showTime - var4) / (float) this.field39922, 0.0F, 1.0F, 1.0F);
        } else {
            return QuadraticEasing.easeOutQuad(var4 / ((float) this.field39922 * 1.4F), 0.0F, 1.0F, 1.0F);
        }
    }

    public float method31994(int var1) {
        float var4 = 0.0F;

        for (int var5 = 0; var5 < var1; var5++) {
            var4 += this.getAnimation(this.notifications.get(var5));
        }

        return var4 / (float) var1;
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (!client.options.hudHidden) {
            for (int var4 = 0; var4 < this.notifications.size(); var4++) {
                Notification notif = this.notifications.get(var4);
                float var6 = this.getAnimation(notif);
                int field39923 = 340;
                int field39926 = 10;
                int var7 = client.getWindow().getWidth() - field39926 - (int) ((float) field39923 * var6 * var6);
                int field39924 = 64;
                int field39925 = 10;
                int field39927 = 10;
                int var8 = client.getWindow().getHeight()
                        - field39924
                        - field39925
                        - var4 * (int) ((float) field39924 * this.method31994(var4) + (float) field39927 * this.method31994(var4));
                float var9 = Math.min(1.0F, var6);
                int var10 = new Color(0.14F, 0.14F, 0.14F, var9 * 0.93F).getRGB();
                int var11 = new Color(0.0F, 0.0F, 0.0F, Math.min(var6 * 0.075F, 1.0F)).getRGB();
                int var12 = new Color(1.0F, 1.0F, 1.0F, var9).getRGB();
                RenderUtils.drawRoundedRect((float) var7, (float) var8, (float) field39923, (float) field39924, 10.0F, var9);
                RenderUtils.drawRoundedRect((float) var7, (float) var8, (float) (var7 + field39923), (float) (var8 + field39924), var10);
                RenderUtils.drawRoundedRect((float) var7, (float) var8, (float) (var7 + field39923), (float) (var8 + 1), var11);
                RenderUtils.drawRoundedRect((float) var7, (float) (var8 + field39924 - 1), (float) (var7 + field39923), (float) (var8 + field39924), var11);
                RenderUtils.drawRoundedRect((float) var7, (float) (var8 + 1), (float) (var7 + 1), (float) (var8 + field39924 - 1), var11);
                RenderUtils.drawRoundedRect(
                        (float) (var7 + field39923 - 1), (float) (var8 + 1), (float) (var7 + field39923), (float) (var8 + field39924 - 1), var11
                );
                ScissorUtils.startScissor(var7, var8, var7 + field39923 - field39927, var8 + field39924);
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_20, (float) (var7 + field39924 + field39927 - 2), (float) (var8 + field39927), notif.title, var12
                );
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_14,
                        (float) (var7 + field39924 + field39927 - 2),
                        (float) (var8 + field39927 + FontUtils.HELVETICA_LIGHT_20.getHeight(notif.title)),
                        notif.desc,
                        var12
                );
                ScissorUtils.restoreScissor();
                RenderUtils.drawImage(
                        (float) (var7 + field39927 / 2),
                        (float) (var8 + field39927 / 2),
                        (float) (field39924 - field39927),
                        (float) (field39924 - field39927),
                        notif.icon
                );
            }
        }
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        this.notifications.removeIf(var5 -> var5.time.getElapsedTime() > (long) var5.showTime);
    }

    public boolean isRendering() {
        for (Notification notification : this.notifications) {
            if (this.getAnimation(notification) > 0) {
                return true;
            }
        }
        return false;
    }

}
