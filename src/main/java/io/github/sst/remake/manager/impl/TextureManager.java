package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Priority;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.render.Render2DEvent;
import io.github.sst.remake.manager.Manager;
import org.newdawn.slick.opengl.texture.Texture;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class TextureManager extends Manager {

    public final List<Texture> textures = new ArrayList<>();

    public void add(Texture texture) {
        textures.add(texture);
    }

    @Subscribe(priority = Priority.HIGH)
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
