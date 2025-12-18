package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.interfaces.ICustomGuiScreenVisitor;

public class Class7262 implements ICustomGuiScreenVisitor {
    public int field31151;
    public int field31152;

    public Class7262(int var1) {
        this(var1, 0);
    }

    public Class7262(int var1, int var2) {
        this.field31151 = var1;
        this.field31152 = var2;
    }

    private CustomGuiScreen method22797(CustomGuiScreen[] var1) {
        CustomGuiScreen var4 = var1[0];

        for (CustomGuiScreen var8 : var1) {
            if (var8.getHeight() > var4.getHeight()) {
                var4 = var8;
            }
        }

        return var4;
    }

    @Override
    public void visit(CustomGuiScreen var1) {
        if (var1.getChildren().size() > 0) {
            int var4 = 0;

            while (var4 < var1.getChildren().size()) {
                CustomGuiScreen var5 = var1.getChildren().get(var4);
                if (var4 > 0 && var4 % this.field31151 == 0) {
                    CustomGuiScreen[] var6 = new CustomGuiScreen[this.field31151];

                    for (int var7 = 0; var7 < this.field31151; var7++) {
                        var6[var7] = var1.getChildren().get(var4 - this.field31151 + var7);
                    }

                    CustomGuiScreen var9 = this.method22797(var6);
                    var5.setSize((var2, var3) -> var2.setY(var9.getY() + var2.getHeight() + this.field31152));
                }

                CustomGuiScreen[] var8 = new CustomGuiScreen[this.field31151];
                var8[0] = var5;

                for (int var10 = 1; var10 < this.field31151; var10++) {
                    var8[var10] = var1.getChildren().get(var4 + var10);
                    this.method22797(var8).setSize((var1x, var2) -> var1x.setY(var5.getY() + var5.getHeight() / 2));
                }

                var4 += this.field31151;
            }
        }
    }
}
