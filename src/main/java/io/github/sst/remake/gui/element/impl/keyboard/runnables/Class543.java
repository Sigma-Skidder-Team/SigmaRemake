package io.github.sst.remake.gui.element.impl.keyboard.runnables;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.keyboard.PopOver;
import io.github.sst.remake.gui.impl.JelloKeyboard;

public class Class543 implements Runnable {
    public final JelloKeyboard parent;

    public Class543(JelloKeyboard parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        for (CustomGuiScreen var4 : this.parent.getChildren()) {
            if (var4 instanceof PopOver) {
                PopOver var5 = (PopOver) var4;
                var5.method13712();
                this.parent.field20957.method13104();
                var5.setReAddChildren(true);
                var5.method13242();
                this.parent.method13234(this.parent.field20960);
            }
        }
    }
}
