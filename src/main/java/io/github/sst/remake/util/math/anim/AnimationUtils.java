package io.github.sst.remake.util.math.anim;

import lombok.Getter;

import java.util.Date;

public class AnimationUtils {
    public int duration;
    public int reverseDuration;
    @Getter
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

    public static float easeOutCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        elapsedTime--; // equivalent to --t in classic easing formula
        return change * (elapsedTime * elapsedTime * elapsedTime + 1.0F) + startValue;
    }

    public static float easeInCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        return change * elapsedTime * elapsedTime * elapsedTime + startValue;
    }
}