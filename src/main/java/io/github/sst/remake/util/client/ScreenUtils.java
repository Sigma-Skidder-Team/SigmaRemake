package io.github.sst.remake.util.client;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.impl.JelloOptions;
import io.github.sst.remake.gui.screen.KeybindsScreen;
import io.github.sst.remake.gui.impl.JelloMenu;
import io.github.sst.remake.gui.impl.JelloKeyboard;
import io.github.sst.remake.gui.screen.OptionsScreen;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gui.screen.TitleScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ScreenUtils implements IMinecraft {

    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, Class<? extends Screen>> replacementScreens = new HashMap<>();
    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, String> screenToScreenName = new HashMap<>();

    static {
        // Minecraft Screen -> Sigma Screen
        replacementScreens.put(TitleScreen.class, JelloMenu.class);
        replacementScreens.put(OptionsScreen.class, JelloOptions.class);
        replacementScreens.put(KeybindsScreen.class, JelloKeyboard.class);

        // Minecraft Screen -> Screen Name
        screenToScreenName.put(KeybindsScreen.class, "Jello Keyboard");
    }

    public static Screen mcToSigma(net.minecraft.client.gui.screen.Screen screen) {
        if (screen == null) {
            return null;
        } else if (!replacementScreens.containsKey(screen.getClass())) {
            return null;
        } else {
            try {
                return replacementScreens.get(screen.getClass()).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                Client.LOGGER.error("Error creating replacement screen", e);
            }

            return null;
        }
    }

    public static Class<? extends net.minecraft.client.gui.screen.Screen> getScreenByName(String name) {
        for (Map.Entry var5 : screenToScreenName.entrySet()) {
            if (name.equals(var5.getValue())) {
                return (Class<? extends net.minecraft.client.gui.screen.Screen>) var5.getKey();
            }
        }

        return null;
    }

    public static String getNameForTarget(Class<? extends net.minecraft.client.gui.screen.Screen> screen) {
        if (screen != null) {
            for (Map.Entry var5 : screenToScreenName.entrySet()) {
                if (screen == var5.getKey()) {
                    return (String) var5.getValue();
                }
            }
        }

        return "";
    }

    public static boolean hasReplacement(net.minecraft.client.gui.screen.Screen screen) {
        return replacementScreens.containsKey(screen.getClass());
    }

}
