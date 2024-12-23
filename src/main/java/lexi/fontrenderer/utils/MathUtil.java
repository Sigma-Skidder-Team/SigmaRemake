package lexi.fontrenderer.utils;

public class MathUtil {

    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;

    private static int ceil(float x) {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
    }

    public static int ceiling_double_int(double value) {
        return ceil((float) value);
    }

}
