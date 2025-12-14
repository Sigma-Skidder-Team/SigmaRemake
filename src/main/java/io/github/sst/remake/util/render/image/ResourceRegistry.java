package io.github.sst.remake.util.render.image;

import io.github.sst.remake.util.render.font.DefaultClientFont;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.InputStream;

public class ResourceRegistry {
    public static final TrueTypeFont JelloLightFont12 = getFont("font/helvetica-neue-light.ttf", 0, 12.0F);
    public static final TrueTypeFont JelloLightFont14 = getFont("font/helvetica-neue-light.ttf", 0, 14.0F);
    public static final TrueTypeFont JelloLightFont18 = getFont("font/helvetica-neue-light.ttf", 0, 18.0F);
    public static final TrueTypeFont JelloLightFont20 = getFont("font/helvetica-neue-light.ttf", 0, 20.0F);
    public static final TrueTypeFont JelloLightFont25 = getFont("font/helvetica-neue-light.ttf", 0, 25.0F);
    public static final TrueTypeFont JelloLightFont40 = getFont("font/helvetica-neue-light.ttf", 0, 40.0F);
    public static final TrueTypeFont JelloLightFont50 = getFont("font/helvetica-neue-light.ttf", 0, 50.0F);
    public static final TrueTypeFont JelloLightFont28 = getFont("font/helvetica-neue-light.ttf", 0, 28.0F);
    public static final TrueTypeFont JelloLightFont24 = getFont("font/helvetica-neue-light.ttf", 0, 24.0F);
    public static final TrueTypeFont JelloLightFont36 = getFont("font/helvetica-neue-light.ttf", 0, 36.0F);
    public static final TrueTypeFont RegularFont20 = getFont("font/regular.ttf", 0, 20.0F);
    public static final TrueTypeFont RegularFont40 = getFont("font/regular.ttf", 0, 40.0F);
    public static final TrueTypeFont JelloMediumFont20 = getFont("font/helvetica-neue medium.ttf", 0, 20.0F);
    public static final TrueTypeFont JelloMediumFont25 = getFont("font/helvetica-neue medium.ttf", 0, 25.0F);
    public static final TrueTypeFont JelloMediumFont40 = getFont("font/helvetica-neue medium.ttf", 0, 40.0F);
    public static final TrueTypeFont JelloMediumFont50 = getFont("font/helvetica-neue medium.ttf", 0, 50.0F);
    public static final DefaultClientFont DefaultClientFont = new DefaultClientFont(2);
    public static final TrueTypeFont JelloLightFont18_1 = getFont2("font/helvetica-neue-light.ttf", 0, 18.0F);
    public static final TrueTypeFont JelloMediumFont20_1 = getFont2("font/helvetica-neue medium.ttf", 0, 20.0F);

    public static TrueTypeFont getFont2(String fontPath, int style, float size) {
        try {
            InputStream fontFile = Resources.readInputStream(fontPath);
            Font font = Font.createFont(0, fontFile);
            font = font.deriveFont(style, size);
            return new TrueTypeFont(font, (int) size);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }

    public static TrueTypeFont getFont(String fontPath, int style, float size) {
        try {
            InputStream fontFile = Resources.readInputStream(fontPath);
            Font font = Font.createFont(0, fontFile);
            font = font.deriveFont(style, size);
            return new TrueTypeFont(font, true);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }
}
