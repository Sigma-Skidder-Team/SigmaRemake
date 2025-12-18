package io.github.sst.remake.util.math.anim;

import io.github.sst.remake.util.math.RandomIntGenerator;
import io.github.sst.remake.util.math.TimerUtils;
import lombok.Getter;

public class AnimationManager {
    @Getter
    private float currentValue;
    private final RandomIntGenerator random = new RandomIntGenerator();
    private final TimerUtils timer = new TimerUtils();
    private long nextInterval;
    private boolean isAnimating = false;
    private float targetValue = -1.0F;

    public AnimationManager() {
        this.timer.start();
        this.nextInterval = this.random.nextInt(8000, 10000);
        this.currentValue = this.random.nextFloat();
    }

    public void update() {
        if (this.timer.getElapsedTime() > this.nextInterval) {
            this.nextInterval = this.random.nextInt(8000, 10000);
            this.isAnimating = true;
            this.targetValue = this.random.nextFloat() + 0.75F;
            boolean shouldInvert = this.random.nextBoolean();
            if (shouldInvert) {
                this.targetValue *= -1.0F;
            }

            this.timer.reset();
        }

        if (this.isAnimating && this.targetValue != -1.0F && this.timer.getElapsedTime() % 10L == 0L) {
            if (!(this.targetValue > this.currentValue)) {
                this.currentValue -= 0.02F;
                if (this.targetValue > this.currentValue) {
                    this.currentValue = this.targetValue;
                    this.isAnimating = false;
                    this.targetValue = -1.0F;
                }
            } else {
                this.currentValue += 0.02F;
                if (this.targetValue < this.currentValue) {
                    this.currentValue = this.targetValue;
                    this.isAnimating = false;
                    this.targetValue = -1.0F;
                }
            }
        }
    }
}
