package io.github.sst.remake.manager.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.KeyPressEvent;
import io.github.sst.remake.event.impl.client.MouseHoverEvent;
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

    public void save(JsonObject object) {
        JsonObject moduleBinds = new JsonObject();
        for (Module module : Client.INSTANCE.moduleManager.modules) {
            if (module.getKeycode() != 0) {
                moduleBinds.addProperty(module.getName(), module.getKeycode());
            }
        }
        object.add("Modules", moduleBinds);

        JsonObject screenBinds = new JsonObject();
        for (Map.Entry<Class<? extends Screen>, Integer> entry : BindUtils.SCREEN_BINDINGS.entrySet()) {
            String screenName = ScreenUtils.getNameForTarget(entry.getKey());
            if (screenName != null && !screenName.isEmpty()) {
                screenBinds.addProperty(screenName, entry.getValue());
            }
        }
        object.add("Screens", screenBinds);
    }

    public void load(JsonObject object) {
        if (object.has("Modules")) {
            JsonObject moduleBinds = object.getAsJsonObject("Modules");
            for (Map.Entry<String, JsonElement> entry : moduleBinds.entrySet()) {
                Module module = Client.INSTANCE.moduleManager.getModule(entry.getKey());
                if (module != null) {
                    module.setKeycode(entry.getValue().getAsInt());
                }
            }
        }

        if (object.has("Screens")) {
            JsonObject screenBinds = object.getAsJsonObject("Screens");
            for (Map.Entry<String, JsonElement> entry : screenBinds.entrySet()) {
                Class<? extends Screen> screenClass = ScreenUtils.getScreenByName(entry.getKey());
                if (screenClass != null) {
                    BindUtils.SCREEN_BINDINGS.put(screenClass, entry.getValue().getAsInt());
                } else {
                    Client.LOGGER.warn("Could not find screen class for bind: " + entry.getKey());
                }
            }
        }
    }

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
}
