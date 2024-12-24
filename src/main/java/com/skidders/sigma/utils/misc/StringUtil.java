package com.skidders.sigma.utils.misc;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class StringUtil {

    public static String convertKeyToName(int keyCode) {
        for (Keys key : Keys.values()) {
            if (key.keyCode == keyCode) {
                return key.name;
            }
        }

        InputUtil.Key input = InputUtil.fromKeyCode(keyCode, 0);
        String[] translationKeyParts = input.getTranslationKey().split("\\.");

        if (translationKeyParts.length > 0) {
            String keyName = translationKeyParts[translationKeyParts.length - 1];

            if (!keyName.isEmpty()) {
                String prefix = keyCode <= 4 ? "Mouse " : "";
                return prefix + keyName.substring(0, 1).toUpperCase() + keyName.substring(1);
            }
        }

        return "Unknown";
    }

}
