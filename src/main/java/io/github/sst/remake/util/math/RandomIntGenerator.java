package io.github.sst.remake.util.math;

import java.util.Random;

public class RandomIntGenerator extends Random {
    public int nextInt(int min, int max) {
        return super.nextInt(max - min) + min;
    }
}