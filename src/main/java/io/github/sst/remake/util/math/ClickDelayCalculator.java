package io.github.sst.remake.util.math;

import lombok.Setter;

/**
 * @author Avitld
 * @link <a href="https://github.com/OSClicker/OSClicker">OSClicker Github</a>
 */
@Setter
public class ClickDelayCalculator {
    private double currentCPS = 10.0;
    private double targetCPS = 10.0;
    private long lastCpsUpdateTime = System.currentTimeMillis();

    // Configurable delay patterns (set externally)
    private double delayPattern1 = 90;
    private double delayPattern2 = 110;
    private double delayPattern3 = 130;
    private Boolean patternEnabled = false;

    // CPS range
    private double minCPS;
    private double maxCPS;

    public void setMinMax(double minCPS, double maxCPS) {
        this.minCPS = minCPS;
        this.maxCPS = maxCPS;
    }

    public ClickDelayCalculator(double minCPS, double maxCPS) {
        this.minCPS = minCPS;
        this.maxCPS = maxCPS;
    }

    public long getClickDelay() {
        long now = System.currentTimeMillis();

        long changeInterval = 2000; // how often to change target CPS
        if (now - lastCpsUpdateTime > changeInterval) {
            lastCpsUpdateTime = now;
            targetCPS = minCPS + Math.random() * (maxCPS - minCPS);
        }

        double smoothingFactor = 0.05;
        currentCPS += (targetCPS - currentCPS) * smoothingFactor;

        double delay = 1000.0 / currentCPS;

        if (patternEnabled) {
            int pattern = 1 + (int) (Math.random() * 3);
            switch (pattern) {
                case 1:
                    delay = delayPattern1;
                    break;
                case 2:
                    delay = delayPattern2;
                    break;
                case 3:
                    delay = delayPattern3;
                    break;
            }
        }

        return (long) delay;
    }
}