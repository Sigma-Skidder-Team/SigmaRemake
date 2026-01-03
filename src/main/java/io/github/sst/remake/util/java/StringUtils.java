package io.github.sst.remake.util.java;

import org.apache.commons.codec.binary.Base64;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class StringUtils {
    private static final String[] goodbyeTitles = new String[]{
            "Goodbye.",
            "See you soon.",
            "Bye!",
            "Au revoir",
            "See you!",
            "Ciao!",
            "Adios",
            "Farewell",
            "See you later!",
            "Have a good day!",
            "See you arround.",
            "See you tomorrow!",
            "Goodbye, friend.",
            "Logging out.",
            "Signing off!",
            "Shutting down.",
            "Was good to see you!"
    };

    private static final String[] goodbyeMessages = new String[]{
            "The two hardest things to say in life are hello for the first time and goodbye for the last.",
            "Don’t cry because it’s over, smile because it happened.",
            "It’s time to say goodbye, but I think goodbyes are sad and I’d much rather say hello. Hello to a new adventure.",
            "We’ll meet again, Don’t know where, don’t know when, But I know we’ll meet again, some sunny day.",
            "This is not a goodbye but a 'see you soon'.",
            "You are my hardest goodbye.",
            "Goodbyes are not forever, are not the end; it simply means I’ll miss you until we meet again.",
            "Good friends never say goodbye. They simply say \"See you soon\".",
            "Every goodbye always makes the next hello closer.",
            "Where's the good in goodbye?",
            "And I'm sorry, so sorry. But, I have to say goodbye."
    };

    public static final String RANDOM_GOODBYE_TITLE = goodbyeTitles[new Random().nextInt(goodbyeTitles.length)];
    public static final String RANDOM_GOODBYE_MESSAGE = goodbyeMessages[new Random().nextInt(goodbyeMessages.length)];

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

    public static String encode(String value) {
        if (value == null) {
            return "";
        }

        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}
