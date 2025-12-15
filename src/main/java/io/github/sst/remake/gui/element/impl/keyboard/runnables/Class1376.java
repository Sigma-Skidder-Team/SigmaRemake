package io.github.sst.remake.gui.element.impl.keyboard.runnables;

import io.github.sst.remake.gui.impl.JelloKeyboard;

public class Class1376 implements Runnable {
    public final JelloKeyboard parent;

    public Class1376(JelloKeyboard parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        this.parent.field20957.method13242();
        this.parent.clearChildren();
        this.parent.field20961 = 0;
    }
}
