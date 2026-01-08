package anightdazingzoroark.riftlib.util;

import org.lwjgl.util.vector.Quaternion;

public class QuaternionUtils {
    public static Quaternion createXYZQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternion(
                (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
        );
    }

    public static Quaternion createYXZQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternion(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ + sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );
    }

    public static Quaternion createYZXQuaternion(double rotationX, double rotationY, double rotationZ) {
        double cosY = Math.cos(rotationY / 2);
        double sinY = Math.sin(rotationY / 2);
        double cosX = Math.cos(rotationX / 2);
        double sinX = Math.sin(rotationX / 2);
        double cosZ = Math.cos(rotationZ / 2);
        double sinZ = Math.sin(rotationZ / 2);

        return new Quaternion(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );
    }
}
