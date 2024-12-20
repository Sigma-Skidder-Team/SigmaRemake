package info.opensigma.util.math

/**
 * A set of methods used for helping with geometry.
 */
object GeometryUtils {

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
    fun isInBounds(x: Double, y: Double, left: Double, up: Double, right: Double, down: Double, relative: Boolean) =
        if (relative) x in left..(left + right) && y in up..(up + down)
        else x in left..right && y in up..down

}