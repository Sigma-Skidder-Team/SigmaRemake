package io.github.sst.remake.gui.framework.core;

import io.github.sst.remake.gui.framework.event.InteractiveWidgetHandler;
import io.github.sst.remake.util.math.color.ColorHelper;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class InteractiveWidget extends Widget {
    private final List<InteractiveWidgetHandler> pressHandlers = new ArrayList<>();

    public InteractiveWidget(GuiComponent parent, String name, int x, int y, int width, int height, boolean draggable) {
        super(parent, name, x, y, width, height, draggable);
    }

    public InteractiveWidget(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper color, boolean draggable) {
        super(parent, name, x, y, width, height, color, draggable);
    }

    public InteractiveWidget(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper color,
            String text,
            boolean draggable
    ) {
        super(parent, name, x, y, width, height, color, text, draggable);
    }

    public InteractiveWidget(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper textColor,
            String text,
            TrueTypeFont font,
            boolean draggable
    ) {
        super(parent, name, x, y, width, height, textColor, text, font, draggable);
    }

    public void onPress(InteractiveWidgetHandler handler) {
        this.pressHandlers.add(handler);
    }

    public void firePressHandlers() {
        for (InteractiveWidgetHandler handler : this.pressHandlers) {
            handler.handle(this);
        }
    }
}
