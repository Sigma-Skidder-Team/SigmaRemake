package com.skidders.sigma.util.system;

import com.skidders.sigma.util.system.math.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class SmoothInterpolator {
    private final double smoothnessFactor;

    public SmoothInterpolator(double smoothnessFactor) {
        if (!(smoothnessFactor <= 0.0) && !(smoothnessFactor >= 1.0)) {
            this.smoothnessFactor = smoothnessFactor;
        } else {
            throw new AssertionError("Smoothness must be between 0 and 1 (both non-inclusive)");
        }
    }

    public static float interpolate(float t, double... points) {
        ArrayList<Vector2d> vectorPoints = new ArrayList<>();
        vectorPoints.add(new Vector2d(0.0, 0.0));
        vectorPoints.add(new Vector2d(points[0], points[1]));
        vectorPoints.add(new Vector2d(points[2], points[3]));
        vectorPoints.add(new Vector2d(1.0, 1.0));
        SmoothInterpolator smoothInterpolator = new SmoothInterpolator(0.0055555557F);
        return (float) smoothInterpolator.calculateInterpolatedValue(vectorPoints, t);
    }

    public double calculateInterpolatedValue(List<Vector2d> points, float t) {
        if (t == 0.0F) {
            return 0.0;
        } else {
            List<Vector2d> processedPoints = this.generateInterpolatedPoints(points);  // This method is inferred to process the points in some way.
            double result = 1.0;

            for (int i = 0; i < processedPoints.size(); i++) {
                Vector2d currentPoint = processedPoints.get(i);
                if (!(currentPoint.x() <= (double) t)) {
                    break;
                }

                result = currentPoint.y();  // Inferred that this might be the interpolation value.
                Vector2d nextPoint = new Vector2d(1.0, 1.0);
                if (i + 1 < processedPoints.size()) {
                    nextPoint = processedPoints.get(i + 1);
                }

                double deltaX = nextPoint.x() - currentPoint.x();
                double deltaY = nextPoint.y() - currentPoint.y();
                double deltaT = (double) t - currentPoint.x();
                double ratio = deltaT / deltaX;
                result += deltaY * ratio;
            }

            return result;
        }
    }

    public List<Vector2d> generateInterpolatedPoints(List<Vector2d> controlPoints) {
        if (controlPoints != null) {
            if (controlPoints.size() >= 3) {
                Vector2d p0 = controlPoints.get(0);
                Vector2d p1 = controlPoints.get(1);
                Vector2d p2 = controlPoints.get(2);
                Vector2d p3 = controlPoints.size() != 4 ? null : controlPoints.get(3);
                ArrayList<Vector2d> interpolatedPoints = new ArrayList<>();
                Vector2d currentPoint = p0;
                double t = 0.0;

                while (t < 1.0) {
                    interpolatedPoints.add(currentPoint);
                    currentPoint = p3 != null ? calculateCubicInterpolation(p0, p1, p2, p3, t) : calculateQuadraticInterpolation(p0, p1, p2, t);
                    t += this.smoothnessFactor;
                }

                return interpolatedPoints;
            } else {
                return null; // Not enough points to interpolate
            }
        } else {
            throw new AssertionError("Provided list had no reference");
        }
    }

    public Vector2d calculateCubicInterpolation(Vector2d p0, Vector2d p1, Vector2d p2, Vector2d p3, double t) {
        double oneMinusT = 1.0 - t;
        double oneMinusTSquared = oneMinusT * oneMinusT;
        double oneMinusTCubed = oneMinusTSquared * oneMinusT;

        double x = p0.x() * oneMinusTCubed
                + p1.x() * 3.0 * t * oneMinusTSquared
                + p2.x() * 3.0 * t * t * oneMinusT
                + p3.x() * t * t * t;

        double y = p0.y() * oneMinusTCubed
                + p1.y() * 3.0 * t * oneMinusTSquared
                + p2.y() * 3.0 * t * t * oneMinusT
                + p3.y() * t * t * t;

        return new Vector2d(x, y);
    }

    public Vector2d calculateQuadraticInterpolation(Vector2d p0, Vector2d p1, Vector2d p2, double t) {
        double oneMinusT = 1.0 - t;
        double oneMinusTSquared = oneMinusT * oneMinusT;

        double x = oneMinusTSquared * p0.x() + 2.0 * t * oneMinusT * p1.x() + t * t * p2.x();
        double y = oneMinusTSquared * p0.y() + 2.0 * t * oneMinusT * p1.y() + t * t * p2.y();

        return new Vector2d(x, y);
    }
}
