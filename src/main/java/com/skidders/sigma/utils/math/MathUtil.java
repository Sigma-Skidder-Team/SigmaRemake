package com.skidders.sigma.utils.math;

public class MathUtil {

    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;

    public static int ceil(float x) {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
    }

    public static int ceiling_float_int(float value) {
        return ceil(value);
    }

    public static int ceiling_double_int(double value) {
        return ceil((float) value);
    }

}
