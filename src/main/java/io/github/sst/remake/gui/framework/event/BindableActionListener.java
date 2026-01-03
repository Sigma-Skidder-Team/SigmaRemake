package io.github.sst.remake.gui.framework.event;

import io.github.sst.remake.gui.screen.keyboard.BindableAction;
import io.github.sst.remake.gui.screen.keyboard.ActionSelectionPanel;

public interface BindableActionListener {
    void onBindableActionSelected(ActionSelectionPanel panel, BindableAction action);
}
