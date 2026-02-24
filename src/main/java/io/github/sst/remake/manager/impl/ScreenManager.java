package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.OpenScreenEvent;
import io.github.sst.remake.event.impl.game.RunLoopEvent;
import io.github.sst.remake.event.impl.window.*;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ScreenUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ScreenManager extends Manager implements IMinecraft {
    public int[] mousePositions = new int[2];
    public float scaleFactor = 1.0F;
    public double mouseScroll;

    public Screen currentScreen;

    private List<Integer> keysPressed;
    private List<Integer> modifiersPressed;
    private List<Integer> mouseButtonsPressed;
    private List<Integer> mouseButtonsReleased;
    private List<Integer> charsTyped;

    @Override
    public void init() {
        keysPressed = new ArrayList<>();
        modifiersPressed = new ArrayList<>();
        mouseButtonsPressed = new ArrayList<>();
        mouseButtonsReleased = new ArrayList<>();
        charsTyped = new ArrayList<>();

        GLFW.glfwSetCursor(client.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
        scaleFactor = (float) (client.getWindow().getFramebufferHeight() / client.getWindow().getHeight());
        super.init();
    }

    @Subscribe
    public void onRunLoop(RunLoopEvent event) {
        if (event.isPost() && this.currentScreen != null) {
            this.mousePositions[0] = Math.max(0, Math.min(client.getWindow().getWidth(), (int) client.mouse.getX()));
            this.mousePositions[1] = Math.max(0, Math.min(client.getWindow().getHeight(), (int) client.mouse.getY()));

            for (int key : this.keysPressed) {
                this.currentScreen.keyPressed(key);
            }

            for (int modifier : this.modifiersPressed) {
                this.currentScreen.modifierPressed(modifier);
            }

            if (client.overlay == null) {
                for (int mouseButton : this.mouseButtonsPressed) {
                    this.currentScreen.onMouseDown(this.mousePositions[0], this.mousePositions[1], mouseButton);
                }

                for (int mouseButton : this.mouseButtonsReleased) {
                    this.currentScreen.onMouseRelease(this.mousePositions[0], this.mousePositions[1], mouseButton);
                }
            }

            for (int chr : this.charsTyped) {
                this.currentScreen.charTyped((char) chr);
            }

            this.keysPressed.clear();
            this.modifiersPressed.clear();
            this.mouseButtonsPressed.clear();
            this.mouseButtonsReleased.clear();
            this.charsTyped.clear();

            if (this.mouseScroll != 0.0) {
                if (client.overlay == null) {
                    this.currentScreen.onScroll((float) this.mouseScroll);
                }
                this.mouseScroll = 0.0;
            }

            if (this.currentScreen != null) {
                this.currentScreen.updatePanelDimensions(this.mousePositions[0], this.mousePositions[1]);
            }
        }
    }

    @Subscribe
    public void onResize(WindowResizeEvent ignoredEvent) {
        if (this.currentScreen != null) {
            Client.INSTANCE.configManager.saveScreenConfig(false);

            try {
                this.currentScreen = this.currentScreen.getClass().newInstance();
                Client.INSTANCE.configManager.loadScreenConfig();
            } catch (IllegalAccessException | InstantiationException e) {
                Client.LOGGER.warn("Failed to resize & set screen", e);
            }
        }

        if (client.getWindow().getWidth() != 0 && client.getWindow().getHeight() != 0) {
            scaleFactor = (float) Math.max(
                    client.getWindow().getFramebufferWidth() / client.getWindow().getWidth(),
                    client.getWindow().getFramebufferHeight() / client.getWindow().getHeight()
            );
        }
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        if (currentScreen != null) {
            if (event.action == 1 || event.action == 2) {
                this.keysPressed.add(event.key);
            } else if (event.action == 0) {
                this.modifiersPressed.add(event.key);
            }
            event.cancel();
        }
    }

    @Subscribe
    public void onChar(CharEvent event) {
        if (currentScreen != null) {
            this.charsTyped.add(event.codepoint);
            event.cancel();
        }
    }

    @Subscribe
    public void onMouse(MouseButtonEvent event) {
        if (currentScreen != null) {
            if (event.action != 1) {
                if (event.action == 0) {
                    this.mouseButtonsReleased.add(event.button);
                }
            } else {
                this.mouseButtonsPressed.add(event.button);
            }
            event.cancel();
        }
    }

    @Subscribe
    public void onScroll(MouseScrollEvent event) {
        if (currentScreen != null) {
            this.mouseScroll += event.vertical;
            event.cancel();
        }
    }

    @Subscribe
    public void onOpenScreen(OpenScreenEvent ignoredEvent) {
        handle(ScreenUtils.mcToSigma(client.currentScreen));
    }

    public void handle(Screen screen) {
        Client.INSTANCE.configManager.saveScreenConfig(false);

        this.currentScreen = screen;

        Client.INSTANCE.configManager.loadScreenConfig();

        if (this.currentScreen != null) {
            this.currentScreen.updatePanelDimensions(this.mousePositions[0], this.mousePositions[1]);
        }
    }
}
