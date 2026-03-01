package io.github.sst.remake.util.java;

import java.io.IOException;

public class RandomUtils {
    public static void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static double randomInRange(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static float normalizeValue(float min, float max, float value) {
        return (value - min) / (max - min);
    }

    public static float denormalizeValue(float normalized, float min, float max, float step, int decimals) {
        float steps = Math.abs(max - min) / step;
        float steppedValue = min + normalized * steps * step;

        double scale = Math.pow(10.0, decimals);
        return (float) (Math.round(steppedValue * scale) / scale);
    }

    public static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
