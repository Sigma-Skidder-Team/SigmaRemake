package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.manager.Manager;
import org.newdawn.slick.opengl.texture.Texture;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public final class TextureManager extends Manager {
    public List<Texture> textures;

    @Override
    public void init() {
        textures = new ArrayList<>();
        super.init();
    }

    public void add(Texture texture) {
        textures.add(texture);
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onRender(Render2DEvent event) {
        if (!textures.isEmpty()) {
            try {
                for (Texture texture : textures) {
                    texture.release();
                }

                textures.clear();
            } catch (ConcurrentModificationException exception) {
                Client.LOGGER.warn("Failed to clear texture cache", exception);
            }
        }
    }
}