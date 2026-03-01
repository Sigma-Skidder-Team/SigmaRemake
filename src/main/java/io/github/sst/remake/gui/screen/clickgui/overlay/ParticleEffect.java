package io.github.sst.remake.gui.screen.clickgui.overlay;

import io.github.sst.remake.util.math.RandomIntGenerator;
import io.github.sst.remake.util.math.anim.AnimationManager;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.awt.*;

public class ParticleEffect {
    private float xPosition;
    public float initialXPosition;
    public float yPosition;
    public float size;
    private float movementSpeed;
    private final RandomIntGenerator random = new RandomIntGenerator();
    public float direction;
    public Color color = new Color(1.0F, 1.0F, 1.0F, 0.5F);

    public ParticleEffect(float x, float y) {
        this.initialXPosition = this.xPosition = x;
        this.yPosition = y;
        this.size = (float) this.random.nextInt(1, 3) + this.random.nextFloat();
        this.initialize();
    }

    private void initialize() {
        float maxMovement = 1.0F;
        this.movementSpeed = this.random.nextFloat() % maxMovement;
        this.direction = this.random.nextFloat() / 2.0F;
        if (this.random.nextBoolean()) {
            this.direction *= -1.0F;
        }
    }

    public void render(float partialTicks) {
        RenderUtils.drawCircle(
                this.initialXPosition * 2.0F, this.yPosition * 2.0F, this.size * 2.0F, ColorHelper.applyAlpha(this.color.getRGB(), partialTicks * 0.7F)
        );
    }

    public void updatePosition(AnimationManager animationManager) {
        this.initialXPosition = this.initialXPosition + animationManager.getCurrentValue() + this.direction;
        this.xPosition = this.xPosition + animationManager.getCurrentValue() + this.direction;
        this.yPosition = this.yPosition + this.movementSpeed;
    }
}