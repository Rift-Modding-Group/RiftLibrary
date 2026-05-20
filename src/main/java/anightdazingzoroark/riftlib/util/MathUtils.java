package anightdazingzoroark.riftlib.util;

import java.util.Random;

public class MathUtils {
    public static double randomInRange(double min, double max) {
        return min + new Random().nextDouble() * (max - min);
    }

    public static float randomInRange(float min, float max) {
        return min + new Random().nextFloat() * (max - min);
    }

    public static double slopeResult(int x, boolean clamped, double xMin, double xMax, double yMin, double yMax) {
        return slopeResult((double) x, clamped, xMin, xMax, yMin, yMax);
    }

    public static double slopeResult(double x, boolean clamped, double xMin, double xMax, double yMin, double yMax) {
        double slope = (yMax - yMin) / (xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return Math.clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return Math.clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }

    public static float slopeResult(int x, boolean clamped, float xMin, float xMax, float yMin, float yMax) {
        float slope = (yMax - yMin)/(xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return Math.clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return Math.clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }
}
