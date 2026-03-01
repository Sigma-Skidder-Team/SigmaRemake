package io.github.sst.remake.gui.framework.core;

import com.google.gson.JsonObject;
import io.github.sst.remake.util.IMinecraft;

public abstract class Screen extends GuiComponent implements IMinecraft {
    public Screen(String name) {
        super(null, name, 0, 0, client.getWindow().getWidth(), client.getWindow().getHeight());
    }

    public int getFPS() {
        return 30;
    }

    @Override
    public void loadPersistedConfig(JsonObject config) {
        super.loadPersistedConfig(config);
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
