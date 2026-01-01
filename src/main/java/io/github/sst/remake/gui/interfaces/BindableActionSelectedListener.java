package io.github.sst.remake.gui.interfaces;

import io.github.sst.remake.gui.element.impl.keyboard.BindableAction;
import io.github.sst.remake.gui.element.impl.keyboard.ModsPanel;

public interface BindableActionSelectedListener {
    void onBindableActionSelected(ModsPanel panel, BindableAction action);
}
