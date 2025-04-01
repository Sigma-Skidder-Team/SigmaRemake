package com.skidders.sigma.screen;

import java.util.Date;

public class Animation {
    public int duration;
    public int reverseDuration;
    public Direction direction;
    public Date startTime;
    public Date reverseStartTime;

    public Animation(int duration, int reverseDuration) {
        this(duration, reverseDuration, Direction.FORWARDS);
    }

    public Animation(int duration, int reverseDuration, Direction direction) {
        this.direction = Direction.FORWARDS;
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
        if (direction == Direction.FORWARDS) {
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
        switch (this.direction.ordinal()) {
            case 1: {
                this.startTime = new Date(new Date().getTime() - (long) (progress * this.duration));
                break;
            }
            case 2: {
                this.reverseStartTime = new Date(new Date().getTime() - (long) ((1.0f - progress) * this.reverseDuration));
                break;
            }
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public float calcPercent() {
        if (this.direction == Direction.BACKWARDS) {
            return Math.max(0.0f, 1.0f - Math.min(1.0f, (new Date().getTime() - this.reverseStartTime.getTime()) / (float) this.reverseDuration));
        }
        return Math.min(1.0f, (new Date().getTime() - this.startTime.getTime()) / (float) this.duration);
    }

    public enum Direction {
        FORWARDS,
        BACKWARDS
    }
}
