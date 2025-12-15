package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.client.ScreenUtils;
import net.minecraft.client.gui.screen.Screen;

public class Class6984 {
    public Module module;
    public Class<? extends Screen> screen;

    public Class6984(Module module) {
        this.module = module;
    }

    public Class6984(Class<? extends Screen> screen) {
        this.screen = screen;
    }

    public String method21596() {
        return this.module == null ? ScreenUtils.screenToScreenName.get(this.screen) : this.module.getName();
    }

    public String method21597() {
        return this.module == null ? "Screen" : this.module.getCategory().toString();
    }

    public void setBind(int keycode) {
        if (this.module == null) {
            BindUtils.SCREEN_BINDINGS.put(this.screen, keycode);
        } else {
            this.module.setKeycode(keycode);
        }
    }

    public int getBind() {
        return this.module == null
                ? BindUtils.SCREEN_BINDINGS.get(this.screen)
                : this.module.keycode;
    }
}
