package io.github.sst.remake.util.java;

import org.apache.commons.codec.binary.Base64;
import org.newdawn.slick.TrueTypeFont;

import java.nio.charset.StandardCharsets;

public class StringUtils {
    public static boolean isPrintableCharacter(char character) {
        return character != '\u00A7'   // section sign (§)
                && character >= ' '
                && character != '\u007F'; // DEL
    }

    public static String cut(String input, String content, int cutStart, int cutEnd) {
        cutStart = Math.min(Math.max(0, cutStart), input.length());
        cutEnd = Math.min(Math.max(0, cutEnd), input.length());
        String start = input.substring(0, Math.min(cutStart, cutEnd));
        String end = input.substring(Math.max(cutStart, cutEnd));
        return start + content + end;
    }

    public static String paste(String original, String content, int maxLen) {
        try {
            String start = original.substring(0, maxLen);
            String end = original.substring(maxLen);
            return start + content + end;
        } catch (Exception var7) {
            return original;
        }
    }

    public static int getFittingCharacterCount(String text, TrueTypeFont font, float leftPadding, int maxWidth, float rightPadding) {
        int previousWidth = -1;
        int currentWidth = -1;

        for (int charIndex = 0; charIndex <= text.length(); charIndex++) {
            int widthBefore = font.getWidth(text.substring(0, Math.max(charIndex - 1, 0)));
            int widthNow = font.getWidth(text.substring(0, charIndex));

            if ((float) widthNow > (float) maxWidth - leftPadding - rightPadding) {
                previousWidth = widthBefore;
                currentWidth = widthNow;
                break;
            }
        }

        if ((float) maxWidth - leftPadding - rightPadding >= (float) font.getWidth(text)) {
            currentWidth = font.getWidth(text);
        }

        int closestWidth = Math.abs((float) maxWidth - leftPadding - rightPadding - (float) previousWidth)
                < Math.abs((float) maxWidth - leftPadding - rightPadding - (float) currentWidth)
                ? previousWidth
                : currentWidth;

        int fittingCharacters = text.length();
        for (int i = 0; i < text.length(); i++) {
            if (font.getWidth(text.substring(0, i)) == closestWidth) {
                fittingCharacters = i;
                break;
            }
        }

        if (fittingCharacters > text.length()) {
            fittingCharacters = text.length();
        }

        return fittingCharacters;
    }

    public static String encodeBase64(String input) {
        return new String(Base64.encodeBase64(input.getBytes()), StandardCharsets.UTF_8);
    }

    public static String decodeBase64(String input) {
        return new String(Base64.decodeBase64(input.getBytes()), StandardCharsets.UTF_8);
    }

    public static byte[] parseBase64Binary(String base64String) {
        return Base64.decodeBase64(base64String);
    }
}
