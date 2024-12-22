package info.opensigma.util.font;

import org.newdawn.slick.TrueTypeFont;

import java.io.InputStream;

public class FontLoader {

    public static TrueTypeFont getFont(String fontPath, int style, float size) {
        try {
            InputStream fontFile = readInputStream(fontPath);
            java.awt.Font font = java.awt.Font.createFont(0, fontFile);
            font = font.deriveFont(style, size);
            return new TrueTypeFont(font, true);
        } catch (Exception ex) {
            return new TrueTypeFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, (int) size), true);
        }
    }

    public static InputStream readInputStream(String fileName) {
        try {
            String assetPath = "assets/sigma-reborn/" + fileName;
            InputStream resourceStream = FontLoader.class.getClassLoader().getResourceAsStream(assetPath);

            if (resourceStream != null) {
                return resourceStream;
            } else {
                throw new IllegalStateException("Resource not found: " + assetPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load resource " + fileName + ". Error during resource loading.", e
            );
        }
    }

}
