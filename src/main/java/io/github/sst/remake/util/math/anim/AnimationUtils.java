package io.github.sst.remake.util.math.anim;

import java.util.Date;

public class AnimationUtils {
    public int duration;
    public int reverseDuration;
    public Direction direction;
    public Date startTime;
    public Date reverseStartTime;

    public AnimationUtils(int duration, int reverseDuration) {
        this(duration, reverseDuration, Direction.BACKWARDS);
    }

    public AnimationUtils(int duration, int reverseDuration, Direction direction) {
        this.direction = Direction.BACKWARDS;
        this.duration = duration;
        this.reverseDuration = reverseDuration;
        this.startTime = new Date();
        this.reverseStartTime = new Date();
        this.changeDirection(direction);
    }

    public static float calculateProgressWithReverse(final Date date, final Date date2, final float a, final float a2) {
        return Math.max(0.0f, Math.min(1.0f, Math.min(a, (float) (new Date().getTime() - ((date != null) ? date.getTime() : new Date().getTime()))) / a * (1.0f - Math.min(a2, (float) (new Date().getTime() - ((date2 != null) ? date2.getTime() : new Date().getTime()))) / a2)));
    }

    public static float calculateProgress(final Date date, final float a) {
        return Math.max(0.0f, Math.min(1.0f, Math.min(a, (float) (new Date().getTime() - ((date != null) ? date.getTime() : new Date().getTime()))) / a));
    }

    public static float calculateProgressWithReverse(final Date date, final Date date2, final float max) {
        return calculateProgressWithReverse(date, date2, max, max);
    }

    public static boolean hasTimeElapsed(Date time, float elapsed) {
        return time != null && (float) (new Date().getTime() - time.getTime()) > elapsed;
    }

    public int getDuration() {
        return this.duration;
    }

    public void changeDirection(final Direction direction) {
        if (this.direction == direction) {
            return;
        }
        if (direction == Direction.BACKWARDS) {
            this.startTime = new Date(new Date().getTime() - (long) (this.calcPercent() * this.duration));
        } else {
            this.reverseStartTime = new Date(new Date().getTime() - (long) ((1.0f - this.calcPercent()) * this.reverseDuration));
        }
        this.direction = direction;
    }

    /**
     * Updates the start time of the animation based on the given progress value.
     *
     * @param progress The progress value, ranging from 0.0 to 1.0, representing the current state of the animation.
     * @see #getDirection()
     * @see #getDuration()
     */
    public void updateStartTime(final float progress) {
        switch (this.direction) {
            case BACKWARDS: {
                this.startTime = new Date(new Date().getTime() - (long) (progress * this.duration));
                break;
            }
            case FORWARDS: {
                this.reverseStartTime = new Date(new Date().getTime() - (long) ((1.0f - progress) * this.reverseDuration));
                break;
            }
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public float calcPercent() {
        if (this.direction == Direction.FORWARDS) {
            return Math.max(0.0f, 1.0f - Math.min(1.0f, (new Date().getTime() - this.reverseStartTime.getTime()) / (float) this.reverseDuration));
        }
        return Math.min(1.0f, (new Date().getTime() - this.startTime.getTime()) / (float) this.duration);
    }

    public enum Direction {
        BACKWARDS,
        FORWARDS
    }

    public static float calculateTransition(float var0, float var1, float var2, float var3) {
        var0 /= var3;
        return var2 * (var0 * var0 * --var0 + 1.0F) + var1;
    }

    public static float calculateBackwardTransition(float var0, float var1, float var2, float var3) {
        var0 /= var3;
        return var2 * var0 * var0 * var0 + var1;
    }
}
