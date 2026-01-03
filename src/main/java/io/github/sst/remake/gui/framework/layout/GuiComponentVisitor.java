package io.github.sst.remake.gui.framework.layout;

import io.github.sst.remake.gui.framework.core.GuiComponent;

public interface GuiComponentVisitor {
    void visit(GuiComponent screen);
}
