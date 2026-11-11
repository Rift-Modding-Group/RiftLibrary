package anightdazingzoroark.riftlib.util;

import org.joml.Quaternionf;
import org.jetbrains.annotations.NotNull;

public class QuaternionUtils {
    @NotNull
    public static Quaternionf createXYZQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternionf(
                (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
        );
    }

    @NotNull
    public static Quaternionf createYXZQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternionf(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ + sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );
    }

    @NotNull
    public static Quaternionf createYZXQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternionf(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );
    }

    @NotNull
    public static Quaternionf createZYXQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);

        return new Quaternionf(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ + sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );
    }
}
