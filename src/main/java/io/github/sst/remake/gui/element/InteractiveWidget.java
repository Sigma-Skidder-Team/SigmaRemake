package io.github.sst.remake.gui.element;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.interfaces.IHandler;
import io.github.sst.remake.gui.panel.Widget;
import io.github.sst.remake.util.math.color.ColorHelper;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class InteractiveWidget extends Widget {
    private final List<IHandler> iHandlers = new ArrayList<IHandler>();

    public InteractiveWidget(GuiComponent screen, String typeThingIdk, int x, int y, int width, int height, boolean var7) {
        super(screen, typeThingIdk, x, y, width, height, var7);
    }

    public InteractiveWidget(GuiComponent screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, boolean var8) {
        super(screen, typeThingIdk, x, y, width, height, var7, var8);
    }

    public InteractiveWidget(GuiComponent screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, String text, boolean var9) {
        super(screen, typeThingIdk, x, y, width, height, var7, text, var9);
    }

    public InteractiveWidget(GuiComponent screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, String var8, TrueTypeFont font, boolean var10) {
        super(screen, typeThingIdk, x, y, width, height, var7, var8, font, var10);
    }

    public final void onPress(IHandler iHandler) {
        this.iHandlers.add(iHandler);
    }

    public final void callUIHandlers() {
        for (IHandler handler : this.iHandlers) {
            handler.handle(this);
        }
    }
}
