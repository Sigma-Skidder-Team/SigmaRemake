package io.github.sst.remake.gui.element.impl.keyboard.runnables;

import io.github.sst.remake.gui.element.impl.keyboard.ModsPanel;
import io.github.sst.remake.gui.impl.JelloKeyboard;

public class Class1533 implements Runnable {
    public final JelloKeyboard parent;

    public Class1533(JelloKeyboard parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        this.parent
                .addToList(
                        this.parent.field20960 = new ModsPanel(
                                this.parent, "mods", 0, 0, parent.width, parent.height
                        )
                );
        this.parent.field20960.method13623((var1, var2) -> {
            if (var2 != null) {
                var2.setBind(this.parent.field20957.field20696);
            }

            parent.method13332();
        });
        this.parent.field20960.setReAddChildren(true);
    }
}
