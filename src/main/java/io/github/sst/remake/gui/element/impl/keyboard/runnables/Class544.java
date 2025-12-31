package io.github.sst.remake.gui.element.impl.keyboard.runnables;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.keyboard.PopOver;
import io.github.sst.remake.gui.impl.JelloKeyboard;

public class Class544 implements Runnable {
    public final JelloKeyboard parent;

    public Class544(JelloKeyboard parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        for (CustomGuiScreen child : this.parent.getChildren()) {
            if (child instanceof PopOver) {
                PopOver pop = (PopOver) child;
                pop.method13712();
            }
        }
    }
}
