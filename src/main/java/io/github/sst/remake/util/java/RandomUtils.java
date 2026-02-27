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
}
