package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.input.KeyPressEvent;
import io.github.sst.remake.event.impl.input.MouseHoverEvent;
import io.github.sst.remake.event.impl.window.KeyEvent;
import io.github.sst.remake.event.impl.window.MouseButtonEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.client.ScreenUtils;
import io.github.sst.remake.util.client.bind.Bind;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BindManager extends Manager implements IMinecraft {

    private final Map<Integer, List<Bind>> bindCache = new HashMap<>();

    public List<Bind> getBindedObjects(int key) {
        if (key == -1) {
            return Collections.emptyList();
        }

        // Return cached result if present
        return bindCache.computeIfAbsent(key, this::buildBindsForKey);
    }

    public int getKeybindFor(Class<? extends Screen> screen) {
        Integer key = BindUtils.SCREEN_BINDINGS.get(screen);
        return key != null ? key : -1;
    }

    @Subscribe
    public void onMouse(MouseButtonEvent event) {
        int button = event.button;
        int action = event.action;

        if (client.currentScreen == null) {
            if (action != 1 && action != 2) {
                if (action == 0) {
                    MouseHoverEvent mouseEvent = new MouseHoverEvent(button);
                    mouseEvent.call();
                    if (mouseEvent.cancelled) {
                        event.cancel();
                    }
                }
            } else {
                if (button > 1) {
                    press(button);
                }

                KeyPressEvent keyEvent = new KeyPressEvent(button, action == 2, null);
                keyEvent.call();

                if (keyEvent.cancelled) {
                    event.cancel();
                }
            }
        }
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        int key = event.key;
        int action = event.action;

        if (client.currentScreen != null) {
            if (client.currentScreen instanceof ChatScreen && key == 258) { //TAB KEY
                KeyPressEvent keyPress = new KeyPressEvent(key, action == 2, null);
                keyPress.call();

                if (keyPress.cancelled) {
                    event.cancel();
                }
            }
        } else if (action == 1 || action == 2) {
            press(key);
            KeyPressEvent keyPress = new KeyPressEvent(key, action == 2, null);
            keyPress.call();

            if (keyPress.cancelled) {
                event.cancel();
            }
        } else if (action == 0) {
            new MouseHoverEvent(event.key).call();
        }
    }

    private void press(int key) {
        if (key != -1) {
            for (Bind bind : getBindedObjects(key)) {
                if (bind != null && bind.hasTarget()) {
                    switch (bind.getType()) {
                        case MODULE:
                            bind.getModuleTarget().toggle();
                            break;

                        case SCREEN:
                            try {
                                Screen sigmaScreen = bind.getScreenTarget()
                                        .getDeclaredConstructor(Text.class)
                                        .newInstance(new LiteralText((ScreenUtils.screenToScreenName.get(bind.getScreenTarget()))));
                                if (ScreenUtils.hasReplacement(sigmaScreen)) {
                                    client.openScreen(sigmaScreen);
                                }
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                                     NoSuchMethodException | SecurityException | InstantiationException e) {
                                Client.LOGGER.warn("Failed to press", e);
                            }
                            break;
                    }
                }
            }
        }
    }

    private List<Bind> buildBindsForKey(int key) {
        List<Bind> binds = new ArrayList<>();

        for (Module mod : Client.INSTANCE.moduleManager.modules) {
            if (mod.getKeycode() == key) {
                binds.add(new Bind(key, mod));
            }
        }

        for (Map.Entry<Class<? extends Screen>, Integer> entry
                : BindUtils.SCREEN_BINDINGS.entrySet()) {
            if (entry.getValue() == key) {
                binds.add(new Bind(key, entry.getKey()));
            }
        }

        return binds;
    }

    public void getKeybindsJSONObject(JsonObject obj) throws JsonParseException {
        JsonArray keybinds = new JsonArray();

        // Add module keybinds
        for (Module mod : Client.INSTANCE.moduleManager.modules) {
            int key = mod.getKeycode();
            if (key != -1 && key != 0) {
                // Wrap in a Bind to reuse existing JSON serialization
                keybinds.add(new Bind(key, mod).getKeybindData());
            }
        }

        // Add screen keybinds
        for (Map.Entry<Class<? extends Screen>, Integer> entry : BindUtils.SCREEN_BINDINGS.entrySet()) {
            int key = entry.getValue();
            if (key != -1 && key != 0) {
                keybinds.add(new Bind(key, entry.getKey()).getKeybindData());
            }
        }

        obj.add("keybinds", keybinds);
    }
}
