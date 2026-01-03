package io.github.sst.remake.gui.interfaces;

import io.github.sst.remake.gui.element.InteractiveWidget;

// Naming reason: `callUIHandlers` in `UIBase` calls everything in `uiHandlers` (which is a list of `UIHandler`s)
// and passes itself as the argument
public interface InteractiveWidgetHandler {
   void handle(InteractiveWidget interactiveWidget);
}
