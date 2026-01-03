package io.github.sst.remake.gui;

import com.google.gson.JsonObject;
import io.github.sst.remake.util.IMinecraft;

public abstract class Screen extends GuiComponent implements IMinecraft {
    public Screen(String var1) {
        super(null, var1, 0, 0, client.getWindow().getWidth(), client.getWindow().getHeight());
    }

    public int getFPS() {
        return 30;
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);
        this.setWidth(client.getWindow().getWidth());
        this.setHeight(client.getWindow().getHeight());
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == client.options.keyFullscreen.boundKey.getCode()) {
            client.getWindow().toggleFullscreen();
            client.options.fullscreen = client.getWindow().isFullscreen();
        }

        super.keyPressed(keyCode);
    }
}
