package io.github.sst.remake.gui.framework.event;

import io.github.sst.remake.gui.screen.keyboard.BindableAction;
import io.github.sst.remake.gui.screen.keyboard.ModsPanel;

public interface BindableActionListener {
    void onBindableActionSelected(ModsPanel panel, BindableAction action);
}
