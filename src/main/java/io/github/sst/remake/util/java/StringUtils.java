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

    public static String cut(String var0, String content, int cutStart, int cutEnd) {
        cutStart = Math.min(Math.max(0, cutStart), var0.length());
        cutEnd = Math.min(Math.max(0, cutEnd), var0.length());
        String start = var0.substring(0, Math.min(cutStart, cutEnd));
        String end = var0.substring(Math.max(cutStart, cutEnd));
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

    public static int getStringLen(String text, TrueTypeFont font, float var2, int height, float var4) {
        int var7 = -1;
        int width = -1;

        for (int var9 = 0; var9 <= text.length(); var9++) {
            int var10 = font.getWidth(text.substring(0, Math.max(var9 - 1, 0)));
            int var11 = font.getWidth(text.substring(0, var9));
            if ((float) var11 > (float) height - var2 - var4) {
                var7 = var10;
                width = var11;
                break;
            }
        }

        if ((float) height - var2 - var4 >= (float) font.getWidth(text)) {
            width = font.getWidth(text);
        }

        int len = !(Math.abs((float) height - var2 - var4 - (float) var7) < Math.abs((float) height - var2 - var4 - (float) width)) ? width : var7;

        for (int i = 0; i < text.length(); i++) {
            if (font.getWidth(text.substring(0, i)) == len) {
                len = i;
                break;
            }
        }

        if (len > text.length()) {
            len = text.length();
        }

        return len;
    }

    public static String encodeBase64(String s) {
        byte[] bytes = Base64.encodeBase64(s.getBytes());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String decodeBase64(String s) {
        byte[] bytes = Base64.decodeBase64(s.getBytes());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] parseBase64Binary(String base64String) {
        return Base64.decodeBase64(base64String);
    }
}
