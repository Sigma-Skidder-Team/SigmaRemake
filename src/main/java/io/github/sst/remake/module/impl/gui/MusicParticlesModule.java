package io.github.sst.remake.module.impl.gui;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MusicParticlesModule extends Module {
    private final List<MusicParticle> particles = new ArrayList<>();
    public long lastRenderTime = 0L;

    public MusicParticlesModule() {
        super("MusicParticles", "Shows nice particles when music is playing", Category.GUI);
    }

    @Override
    public void onEnable() {
        this.lastRenderTime = System.nanoTime();
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (client.player == null) return;

        if (!Client.INSTANCE.musicManager.isPlaying()
                || Client.INSTANCE.musicManager.visualizer.isEmpty()) {
            this.lastRenderTime = System.nanoTime();
            return;
        }

        if (Client.INSTANCE.musicManager.amplitudes.isEmpty()) {
            this.lastRenderTime = System.nanoTime();
            return;
        }

        long nanoDelta = System.nanoTime() - this.lastRenderTime;
        float spawnBudget = getSpawnBudget((float) nanoDelta);

        int spawned = 0;
        while (this.particles.size() < 40) {
            this.addParticle();
            if ((float) (spawned++) > spawnBudget) {
                break;
            }
        }

        this.updateParticles(spawnBudget);

        for (MusicParticle particle : this.particles) {
            particle.render();
        }

        this.lastRenderTime = System.nanoTime();
    }

    private void addParticle() {
        this.particles.add(new MusicParticle());
    }

    private void updateParticles(float intensity) {
        Iterator<MusicParticle> iterator = this.particles.iterator();

        while (iterator.hasNext()) {
            MusicParticle particle = iterator.next();
            particle.update(intensity);
            if (particle.isExpired()) {
                iterator.remove();
            }
        }
    }

    private static float getSpawnBudget(float nanoDelta) {
        float deltaIntensity = Math.min(10.0F, Math.max(0.0F, nanoDelta / 1.810361E7F));

        double peakAmplitude = 0.0;
        double amplitudeCeiling = 4750.0;

        int samples = Math.min(3, Client.INSTANCE.musicManager.amplitudes.size());
        for (int i = 0; i < samples; i++) {
            peakAmplitude = Math.max(peakAmplitude,
                    Math.sqrt(Client.INSTANCE.musicManager.amplitudes.get(i)) - 1000.0);
        }

        float spawnBudget = 0.7F + (float) (peakAmplitude / (amplitudeCeiling - 1000.0)) * 8.14F;
        spawnBudget *= deltaIntensity;
        return spawnBudget;
    }

    public static class MusicParticle {
        public final float speedFactor = (float) (0.1F + Math.random() * 0.9F);
        public final float sizeFactor = (float) (0.5 + Math.random() * 0.5);
        public final int startX = (int) ((double) client.getWindow().getWidth() * Math.random());
        public final int startY = (int) ((double) client.getWindow().getHeight() * Math.random());
        public float lifeProgress;

        public void update(float intensity) {
            this.lifeProgress += 0.02F * intensity * this.speedFactor;
        }

        public void render() {
            float scale = 0.3F + this.lifeProgress * 0.7F;

            float alpha;
            if (this.lifeProgress < 0.1F) {
                alpha = this.lifeProgress / 0.1F;
            } else if (this.lifeProgress > 0.75F) {
                alpha = 1.0F - (this.lifeProgress - 0.75F) / 0.25F;
            } else {
                alpha = 1.0F;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef((float) (client.getWindow().getWidth() / 2), (float) (client.getWindow().getHeight() / 2), 0.0F);
            GL11.glScalef(scale, scale, 1.0F);
            GL11.glTranslatef((float) (-client.getWindow().getWidth() / 2), (float) (-client.getWindow().getHeight() / 2), 0.0F);

            int color = Color.getHSBColor((float) (System.currentTimeMillis() % 4000L) / 4000.0F, 0.3F, 1.0F).getRGB();
            float size = 60.0F * this.sizeFactor;
            RenderUtils.drawImage(
                    (float) this.startX - size / 2.0F,
                    (float) this.startY - size / 2.0F,
                    size,
                    size,
                    Resources.PARTICLES,
                    ColorHelper.applyAlpha(color, alpha * 0.9F)
            );

            GL11.glPopMatrix();
        }

        public boolean isExpired() {
            return this.lifeProgress >= 1.0F;
        }
    }
}