package io.github.sst.remake.gui.screen.notifications;

import io.github.sst.remake.util.math.TogglableTimer;
import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.opengl.texture.Texture;

import java.util.Objects;

public class Notification {
    private static final int DEFAULT_SHOW_TIME = 4000;
    public final TogglableTimer timer = new TogglableTimer();

    public final String title;
    public String desc;
    public Texture icon;

    public int showTime;

    public Notification(String title, String desc, int showTime, Texture icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.showTime = showTime;
        this.timer.start();
    }

    public Notification(String title, String desc, Texture icon) {
        this(title, desc, DEFAULT_SHOW_TIME, icon);
    }

    public Notification(String title, String desc, int showTime) {
        this(title, desc, showTime, Resources.INFO_ICON);
    }

    public Notification(String title, String desc) {
        this(title, desc, DEFAULT_SHOW_TIME);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title);
    }
}
