package com.skidders.sigma.utils.misc;

import net.minecraft.client.util.InputUtil;
import java.util.Locale

object StringUtil {
    fun convertKeyToName(keyCode: Int): String {
        for (key in Keys.entries) {
            if (key.keyCode == keyCode) {
                return key.key;
            }
        }

        val input = InputUtil.fromKeyCode(keyCode, 0);
        val translationKeyParts = input.translationKey.split("\\.");

        if (translationKeyParts.count() > 0) {
            val keyName = translationKeyParts[translationKeyParts.count() - 1];

            if (!keyName.isEmpty()) {
                val prefix = if (keyCode <= 4) "Mouse " else "";
                return prefix + keyName.substring(0, 1).uppercase(Locale.getDefault()) + keyName.substring(1);
            }
        }

        return "Unknown";
    }
}
