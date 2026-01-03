package io.github.sst.remake.gui.screen.notifications;

import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.opengl.texture.Texture;

import java.awt.*;

public class Notification {
    public static final int field43601 = 3;
    public static final int DEFAULT_SHOW_TIME = 4000;
    public String title;
    public String desc;
    public Texture icon;
    public int showTime;
    public TimerUtils time = new TimerUtils();
    public Color[] field43608 = new Color[field43601];
    public Color[] field43609 = new Color[field43601];
    public int field43610 = 0;

    public Notification(String title, String desc, int showTime, Texture icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.showTime = showTime;
        this.time.start();
    }

    public Notification(String title, String desc, Texture icon) {
        this(title, desc, DEFAULT_SHOW_TIME, icon);
    }

    public Notification(String title, String desc, int showTime) {
        this(title, desc, showTime, Resources.infoIconPNG);
    }

    public Notification(String title, String desc) {
        this(title, desc, DEFAULT_SHOW_TIME);
    }

    @Override
    public boolean equals(Object var1) {
        return var1 instanceof Notification && ((Notification) var1).title.equals(this.title);
    }
}
