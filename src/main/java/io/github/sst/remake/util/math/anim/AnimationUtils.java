package io.github.sst.remake.util.math.anim;

import lombok.Getter;

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

    public static float calculateProgressWithReverse(Date primaryStartTime, Date secondaryStartTime, float primaryDurationMs, float secondaryDurationMs) {
        return Math.max(0.0f, Math.min(1.0f, Math.min(primaryDurationMs, (float) (new Date().getTime() - ((primaryStartTime != null) ? primaryStartTime.getTime() : new Date().getTime()))) / primaryDurationMs * (1.0f - Math.min(secondaryDurationMs, (float) (new Date().getTime() - ((secondaryStartTime != null) ? secondaryStartTime.getTime() : new Date().getTime()))) / secondaryDurationMs)));
    }

    public static float calculateProgress(Date startTime, float durationMs) {
        return Math.max(0.0f, Math.min(1.0f, Math.min(durationMs, (float) (new Date().getTime() - ((startTime != null) ? startTime.getTime() : new Date().getTime()))) / durationMs));
    }

    public static float easeInOutQuad(float time, float startValue, float changeInValue, float duration) {
        time /= duration / 2.0F;

        if (time < 1.0F) {
            return changeInValue / 2.0F * time * time + startValue;
        }

        time--;
        return -changeInValue / 2.0F * (time * (time - 2.0F) - 1.0F) + startValue;
    }

    public void changeDirection(Direction direction) {
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

    public void updateStartTime(float progress) {
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

    public static float easeOutCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        elapsedTime--; // equivalent to --t in classic easing formula
        return change * (elapsedTime * elapsedTime * elapsedTime + 1.0F) + startValue;
    }

    public static float easeInCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        return change * elapsedTime * elapsedTime * elapsedTime + startValue;
    }

    public enum Direction {
        BACKWARDS,
        FORWARDS
    }
}