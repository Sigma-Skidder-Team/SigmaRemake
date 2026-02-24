package io.github.sst.remake.util.math.timer;

@SuppressWarnings("unused")
public class BasicTimer {
    private long lastTimestamp = System.currentTimeMillis();

    /**
     * Returns the elapsed time in milliseconds since the timer was last reset.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - lastTimestamp;
    }

    /**
     * Resets the timer to the current system time.
     */
    public void reset() {
        lastTimestamp = System.currentTimeMillis();
    }

    /**
     * Checks if a specified time has elapsed and optionally resets the timer.
     *
     * @param duration the duration to check in milliseconds
     * @param reset    whether to reset the timer if the duration has elapsed
     * @return true if the specified duration has elapsed, false otherwise
     */
    public boolean hasElapsed(long duration, boolean reset) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimestamp >= duration) {
            if (reset) {
                this.lastTimestamp = currentTime;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a specified time has elapsed without resetting the timer.
     *
     * @param duration the duration to check in milliseconds
     * @return true if the specified duration has elapsed, false otherwise
     */
    public boolean hasElapsed(long duration) {
        return hasElapsed(duration, false);
    }
}