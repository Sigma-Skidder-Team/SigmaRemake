package io.github.sst.remake.util.client;

import io.github.sst.remake.gui.screen.holder.ClickGuiHolder;
import io.github.sst.remake.gui.screen.holder.SpotlightHolder;
import io.github.sst.remake.util.client.bind.Keys;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

public class BindUtils {

    public static final Map<Class<? extends Screen> , Integer> SCREEN_BINDINGS = new HashMap<>();

    static {
        SCREEN_BINDINGS.put(SpotlightHolder.class, Keys.RIGHT_CONTROL.keycode);
        SCREEN_BINDINGS.put(ClickGuiHolder.class, Keys.RIGHT_SHIFT.keycode);
    }

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
