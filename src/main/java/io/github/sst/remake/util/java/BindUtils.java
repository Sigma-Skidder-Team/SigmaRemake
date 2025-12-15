package io.github.sst.remake.util.java;

import io.github.sst.remake.util.java.bind.Keys;
import net.minecraft.client.util.InputUtil;

public class BindUtils {

    public static String getKeyName(int keycode) {
        for (Keys key : Keys.values()) {
            if (key.keycode == keycode) {
                return key.name;
            }
        }

        InputUtil.Key input = InputUtil.fromKeyCode(keycode, 0);
        String[] translationKeyParts = input.getTranslationKey().split("\\.");

        if (translationKeyParts.length > 0) {
            String keyName = translationKeyParts[translationKeyParts.length - 1];

            if (!keyName.isEmpty()) {
                String prefix = keycode <= 4 ? "Mouse " : "";
                return prefix + keyName.substring(0, 1).toUpperCase() + keyName.substring(1);
            }
        }

        return "Unknown";
    }

}
