package io.github.sst.remake.gui.framework.event;

import io.github.sst.remake.gui.framework.core.InteractiveWidget;

// Naming reason: `callUIHandlers` in `UIBase` calls everything in `uiHandlers` (which is a list of `UIHandler`s)
// and passes itself as the argument
public interface InteractiveWidgetHandler {
   void handle(InteractiveWidget interactiveWidget);
}
