package io.github.sst.remake.gui.interfaces;

import io.github.sst.remake.gui.GuiComponent;

public interface GuiComponentVisitor {
    void visit(GuiComponent screen);
}
