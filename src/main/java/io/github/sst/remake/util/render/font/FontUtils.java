package io.github.sst.remake.util.render.font;

import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.InputStream;

public class FontUtils {
    public static final TrueTypeFont REGULAR_20 = loadAntiAliasedFont("font/regular.ttf", 20.0F);
    public static final TrueTypeFont REGULAR_40 = loadAntiAliasedFont("font/regular.ttf", 40.0F);

    public static final TrueTypeFont HELVETICA_LIGHT_18_BASIC = loadBasicFont("font/helvetica-neue-light.ttf", 18.0F);
    public static final TrueTypeFont HELVETICA_MEDIUM_20_BASIC = loadBasicFont("font/helvetica-neue medium.ttf", 20.0F);

    public static final TrueTypeFont HELVETICA_LIGHT_12 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 12.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_14 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 14.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_18 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 18.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_20 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 20.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_24 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 24.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_25 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 25.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_28 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 28.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_36 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 36.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_40 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 40.0F);
    public static final TrueTypeFont HELVETICA_LIGHT_50 = loadAntiAliasedFont("font/helvetica-neue-light.ttf", 50.0F);

    public static final TrueTypeFont HELVETICA_MEDIUM_20 = loadAntiAliasedFont("font/helvetica-neue medium.ttf", 20.0F);
    public static final TrueTypeFont HELVETICA_MEDIUM_25 = loadAntiAliasedFont("font/helvetica-neue medium.ttf", 25.0F);
    public static final TrueTypeFont HELVETICA_MEDIUM_40 = loadAntiAliasedFont("font/helvetica-neue medium.ttf", 40.0F);
    public static final TrueTypeFont HELVETICA_MEDIUM_50 = loadAntiAliasedFont("font/helvetica-neue medium.ttf", 50.0F);

    public static TrueTypeFont loadBasicFont(String fontPath, float size) {
        try {
            InputStream fontFile = Resources.readInputStream(fontPath);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            font = font.deriveFont(Font.PLAIN, size);
            return new TrueTypeFont(font, (int) size);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }

    public static TrueTypeFont loadAntiAliasedFont(String fontPath, float size) {
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
