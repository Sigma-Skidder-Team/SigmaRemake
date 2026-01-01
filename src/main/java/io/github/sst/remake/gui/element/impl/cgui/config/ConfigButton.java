package io.github.sst.remake.gui.element.impl.cgui.config;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.util.math.color.ColorHelper;

public class ConfigButton extends Button {
    public ConfigButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, var2, var3, var4, var5, var6, var7, var8);
    }

    @Override
    public void draw(float partialTicks) {
        this.getWidthSetters().get(0).setWidth(this, this.parent);
        super.draw(partialTicks);
    }
}
