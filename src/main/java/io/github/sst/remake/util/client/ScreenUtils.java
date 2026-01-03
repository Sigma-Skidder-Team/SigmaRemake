package io.github.sst.remake.util.client;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.impl.*;
import io.github.sst.remake.gui.screen.clickgui.JelloClickGuiScreen;
import io.github.sst.remake.gui.screen.holder.*;
import io.github.sst.remake.gui.screen.keyboard.KeyboardScreen;
import io.github.sst.remake.gui.screen.maps.JelloMapsScreen;
import io.github.sst.remake.gui.screen.options.CreditsScreen;
import io.github.sst.remake.gui.screen.options.OptionsScreen;
import io.github.sst.remake.gui.screen.options.OptionsButtonScreen;
import io.github.sst.remake.gui.screen.mainmenu.MainMenuScreen;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ScreenUtils implements IMinecraft {

    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, Class<? extends Screen>> replacementScreens = new HashMap<>();
    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, String> screenToScreenName = new HashMap<>();

    static {
        // Minecraft Screen (Holder) -> Sigma Screen
        replacementScreens.put(TitleScreen.class, MainMenuScreen.class);
        replacementScreens.put(KeybindsHolder.class, KeyboardScreen.class);
        replacementScreens.put(OptionsHolder.class, OptionsScreen.class);
        replacementScreens.put(CreditsHolder.class, CreditsScreen.class);
        replacementScreens.put(SpotlightHolder.class, JelloSpotlight.class);
        replacementScreens.put(ClickGuiHolder.class, JelloClickGuiScreen.class);
        replacementScreens.put(MapsHolder.class, JelloMapsScreen.class);

        // Sigma Screen -> Screen Name
        screenToScreenName.put(KeybindsHolder.class, "Keybind Manager");
        screenToScreenName.put(SpotlightHolder.class, "Spotlight");
        screenToScreenName.put(ClickGuiHolder.class, "Click GUI");
        screenToScreenName.put(MapsHolder.class, "Maps");
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

    public static boolean isValid(net.minecraft.client.gui.screen.Screen screen) {
        if (screen instanceof GameMenuScreen && !(screen instanceof OptionsButtonScreen)) {
            client.currentScreen = null;
            client.openScreen(new OptionsButtonScreen());
            return true;
        } else {
            return false;
        }
    }

}
