package io.github.sst.remake.gui.screen.bird;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class BirdGameWidget extends Widget {
    private final TreeMap<Long, Double> pipes = new TreeMap<>();

    private float birdY;
    private float birdVelocityY;
    public int score;
    public boolean gameOver;
    private boolean hasStarted;
    private final Set<Long> scoredPipes = new java.util.HashSet<>();

    public BirdGameWidget(GuiComponent parent, String name, int x, int y) {
        super(parent, name, x, y, 700, 512, false);
        this.reset();
    }

    public void reset() {
        this.birdY = 0.5F;
        this.birdVelocityY = 0.0F;
        this.pipes.clear();
        this.score = 0;
        this.scoredPipes.clear();
        this.gameOver = false;
        this.hasStarted = false;
    }

    private void updatePipes() {
        long pipeSpawnDelay = 2200;
        long maxPipeCount = 2;
        if (this.pipes.isEmpty()) {
            this.pipes.put(System.currentTimeMillis() + (pipeSpawnDelay * 2), 0.25 + Math.random() * 0.5);
        }

        for (long lastPipeTime = this.pipes.lastKey(); lastPipeTime < System.currentTimeMillis() + (pipeSpawnDelay * maxPipeCount); lastPipeTime = this.pipes.lastKey()) {
            this.pipes.put(lastPipeTime + pipeSpawnDelay, 0.25 + Math.random() * 0.5);
        }

        this.pipes.entrySet().removeIf(entry -> entry.getKey() < System.currentTimeMillis() - (pipeSpawnDelay * 2));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 32) {
            if (!this.gameOver) {
                if (!this.hasStarted) {
                    this.hasStarted = true;
                }
                this.birdVelocityY = 0.020F;
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (this.hasStarted && !this.gameOver) {
            this.updatePipes();
            float timeStep = 60.0F / (float) MinecraftClient.currentFps;
            float gravityAcceleration = -0.0015F;
            this.birdVelocityY += gravityAcceleration * timeStep;
            if (this.birdVelocityY < -0.022F) {
                this.birdVelocityY = -0.022F;
            }
            this.birdY += this.birdVelocityY * timeStep;
            if (this.birdY >= 1.0F) {
                this.birdY = 1.0F;
                this.birdVelocityY = 0.0F;
            }
        }

        ScissorUtils.startScissor(this);

        for (int i = 0; i < 3; i++) {
            RenderUtils.drawImage((float) (this.x + 288 * i), (float) this.y, 288.0F, 512.0F, Resources.GAME_BACKGROUND);
        }

        float pipeGap = 100.0F;
        float pipeSpawnTime = 2200.0F;
        int groundY = this.height - 112;

        if (this.hasStarted && !this.gameOver) {
            if (this.birdY <= 0.0F) {
                this.gameOver = true;
            }
        }

        float birdLeft = (float) this.x + pipeSpawnTime / 12.0F;
        float birdRight = birdLeft + 40.0F;
        float birdTop = (float) this.y + (float) groundY * (1.0F - this.birdY);
        float birdBottom = birdTop + 20.0F;

        for (Entry<Long, Double> entry : this.pipes.entrySet()) {
            int timeUntilSpawn = (int) (entry.getKey() - System.currentTimeMillis());
            float pipeX = (float) timeUntilSpawn / 12.0F;
            float initialPipeX = pipeSpawnTime / 12.0F;
            float pipeOnScreenX = (float) this.x + initialPipeX + pipeX;
            RenderUtils.drawImage(pipeOnScreenX, this.y - 320 + (int) ((double) groundY * entry.getValue()) - pipeGap / 2.0F, 52.0F, 320.0F, Resources.GAME_PIPE_DOWN, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtils.drawImage(pipeOnScreenX, this.y + (int) ((double) groundY * entry.getValue()) + pipeGap / 2.0F, 52.0F, 320.0F, Resources.GAME_PIPE_UP, ClientColors.LIGHT_GREYISH_BLUE.getColor());

            if (this.hasStarted && !this.gameOver) {
                float pipeLeft = pipeOnScreenX;
                float pipeRight = pipeLeft + 52.0F;
                float topPipeBottomY = (float) this.y + (float) ((int) ((double) groundY * entry.getValue())) - pipeGap / 2.0F;
                float bottomPipeTopY = (float) this.y + (float) ((int) ((double) groundY * entry.getValue())) + pipeGap / 2.0F;
                if (birdRight > pipeLeft && birdLeft < pipeRight && (birdTop < topPipeBottomY || birdBottom > bottomPipeTopY)) {
                    this.gameOver = true;
                }
                Long pipeKey = entry.getKey();
                if (!this.scoredPipes.contains(pipeKey) && birdLeft > pipeRight) {
                    this.score++;
                    this.scoredPipes.add(pipeKey);
                }
            }
        }

        float foregroundAnimation = (this.hasStarted && !this.gameOver) ? (float) (System.currentTimeMillis() % 3400L) / 3400.0F : 0.0F;
        for (int i = 0; i < 4; i++) {
            RenderUtils.drawImage((float) (this.x + 288 * i) - 288.0F * foregroundAnimation, (float) (this.y + groundY), 288.0F, 112.0F, Resources.GAME_FOREGROUND);
        }

        float spriteWidth = (float) Resources.GAME_BIRD.getImageWidth() / 3.0F;
        float spriteHeight = (float) Resources.GAME_BIRD.getImageHeight();
        float textureX = spriteWidth;
        if (this.hasStarted) {
            float velocityThreshold = 0.005F;
            if (this.birdVelocityY > velocityThreshold) {
                textureX = 0.0F;
            } else if (this.birdVelocityY < -velocityThreshold) {
                textureX = spriteWidth * 2.0F;
            }
        }

        float birdDrawX = (float) this.x + pipeSpawnTime / 12.0F;
        float birdDrawY = (float) this.y + (float) groundY * (1.0F - this.birdY);
        float birdWidth = 40.0F;
        float birdHeight = 20.0F;

        GL11.glPushMatrix();
        float rotation = 0.0f;
        if (this.hasStarted) {
            rotation = this.birdVelocityY * -2000.0F; // Flipped sign
            rotation = Math.max(-90.0F, Math.min(30.0F, rotation));
        }
        GL11.glTranslatef(birdDrawX + birdWidth / 2.0F, birdDrawY + birdHeight / 2.0F, 0.0F);
        GL11.glRotatef(rotation, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-(birdDrawX + birdWidth / 2.0F), -(birdDrawY + birdHeight / 2.0F), 0.0F);

        RenderUtils.drawImage(birdDrawX, birdDrawY, birdWidth, birdHeight, Resources.GAME_BIRD, ClientColors.LIGHT_GREYISH_BLUE.getColor(), textureX, 0.0F, spriteWidth, spriteHeight, true);
        GL11.glPopMatrix();

        ScissorUtils.restoreScissor();
        super.draw(partialTicks);
    }
}
