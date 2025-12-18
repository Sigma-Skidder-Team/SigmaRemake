package io.github.sst.remake.util.render.image;

import io.github.sst.remake.util.render.font.DefaultClientFont;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.InputStream;

public class ResourceRegistry {
    public static final TrueTypeFont JelloLightFont12 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 12.0F);
    public static final TrueTypeFont JelloLightFont14 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 14.0F);
    public static final TrueTypeFont JelloLightFont18 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 18.0F);
    public static final TrueTypeFont JelloLightFont20 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 20.0F);
    public static final TrueTypeFont JelloLightFont25 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 25.0F);
    public static final TrueTypeFont JelloLightFont40 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 40.0F);
    public static final TrueTypeFont JelloLightFont50 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 50.0F);
    public static final TrueTypeFont JelloLightFont28 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 28.0F);
    public static final TrueTypeFont JelloLightFont24 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 24.0F);
    public static final TrueTypeFont JelloLightFont36 = getAntiAliasedFont("font/helvetica-neue-light.ttf", 36.0F);
    public static final TrueTypeFont JelloMediumFont20 = getAntiAliasedFont("font/helvetica-neue medium.ttf", 20.0F);
    public static final TrueTypeFont JelloMediumFont25 = getAntiAliasedFont("font/helvetica-neue medium.ttf", 25.0F);
    public static final TrueTypeFont JelloMediumFont40 = getAntiAliasedFont("font/helvetica-neue medium.ttf", 40.0F);
    public static final TrueTypeFont JelloMediumFont50 = getAntiAliasedFont("font/helvetica-neue medium.ttf", 50.0F);
    public static final TrueTypeFont RegularFont20 = getAntiAliasedFont("font/regular.ttf", 20.0F);
    public static final TrueTypeFont RegularFont40 = getAntiAliasedFont("font/regular.ttf", 40.0F);
    public static final TrueTypeFont JelloLightFont18_1 = getBasicFont("font/helvetica-neue-light.ttf", 18.0F);
    public static final TrueTypeFont JelloMediumFont20_1 = getBasicFont("font/helvetica-neue medium.ttf", 20.0F);
    public static final DefaultClientFont DefaultClientFont = new DefaultClientFont(2);

    public static TrueTypeFont getBasicFont(String fontPath, float size) {
        try {
            InputStream fontFile = Resources.readInputStream(fontPath);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            font = font.deriveFont(Font.PLAIN, size);
            return new TrueTypeFont(font, (int) size);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }

    public static TrueTypeFont getAntiAliasedFont(String fontPath, float size) {
        try {
            InputStream fontFile = Resources.readInputStream(fontPath);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            font = font.deriveFont(Font.PLAIN, size);
            return new TrueTypeFont(font, true);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }
}
