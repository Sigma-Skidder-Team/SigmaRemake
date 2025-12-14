package io.github.sst.remake.util.client;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.impl.MainMenuScreen;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ScreenUtils implements IMinecraft {

    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, Class<? extends Screen>>
            replacementScreens = new HashMap<>();

    public static final Map<Class<? extends Screen>, String>
            screenToScreenName = new HashMap<>();

    static {
        replacementScreens.put(TitleScreen.class, MainMenuScreen.class);
    }

    public static Screen mcToSigma(net.minecraft.client.gui.screen.Screen screen) {
        if (screen == null) {
            return null;
        } else if (isValid(screen)) {
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

    private static boolean isValid(net.minecraft.client.gui.screen.Screen screen) {
        return false;
    }

}
