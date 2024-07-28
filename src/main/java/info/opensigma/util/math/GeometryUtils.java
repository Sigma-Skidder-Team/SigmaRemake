package info.opensigma.util.math;

/**
 * A set of methods used for helping with geometry.
 */
public class GeometryUtils {

    /**
     * Checks whether the provided X and Y coordinates are in the provided bounds.
     *
     * @param x The X coordinate to be checked.
     * @param y The Y coordinate to be checked.
     * @param left The upper-left X coordinate of the bounds.
     * @param up The upper-left Y coordinate of the bounds.
     * @param right The bottom-right X coordinate of the bounds.
     * @param down The bottom-right Y coordinate of the bounds.
     * @param relative Whether the right and down points are relative to the left and up points.
     * @return Whether the X and Y coordinates are in the provided bounds.
     */
    public static boolean isInBounds(final double x, final double y, final double left, final double up, final double right, final double down, final boolean relative) {
        if (relative)
            return x >= left && x <= left + right && y >= up && y <= up + down;
        else
            return x >= left && x <= right && y >= up && y <= down;
    }

}