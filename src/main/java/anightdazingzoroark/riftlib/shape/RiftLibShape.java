package anightdazingzoroark.riftlib.shape;

import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

import java.util.Random;

/**
 * This class is to be for use in particle emitter shapes and impact shapes.
 * It supports 2d and 3d shapes and is to be used as a helper class for dealing
 * with shape/volume related calculations.
 * */
public abstract class RiftLibShape {
    /**
     * Floating point tolerance used when checking whether a normalized equation
     * value is exactly on a shape edge or surface.
     * */
    protected static final double EQUATION_EPSILON = 1.0E-6;

    @NotNull
    protected final Vec3d shapeOrigin;
    @NotNull
    protected Quaternion yxzQuat = new Quaternion();
    @NotNull
    protected final Random random = new Random();

    public RiftLibShape(@NotNull Vec3d shapeOrigin) {
        this.shapeOrigin = shapeOrigin;
    }

    @NotNull
    public Vec3d getShapeOrigin() {
        return this.shapeOrigin;
    }

    /**
     * Return the normalized equation value for a world space point.
     * Implementations should convert the point to a centered, unrotated
     * shape space offset before applying shape-specific math.
     * Values less than or equal to 1 are inside the shape, values equal to 1
     * are on the surface, and values greater than 1 are outside the shape.
     * */
    protected abstract double getEquationValue(@NotNull Vec3d point);

    /**
     * Interpret a normalized equation value as inside or on-boundary.
     * If surfaceOnly is false, values less than or equal to 1 are accepted.
     * If surfaceOnly is true, only values close enough to 1 are accepted.
     * */
    protected boolean isEquationValueInside(double equationValue, boolean surfaceOnly) {
        if (surfaceOnly) return Math.abs(equationValue - 1.0) <= EQUATION_EPSILON;
        return equationValue <= 1.0;
    }

    /**
     * Convert a world space point into shape space.
     * The returned vector is centered on shapeOrigin and inverse-rotated by yxzQuat.
     * */
    @NotNull
    protected Vec3d getUnrotatedShapeOffset(@NotNull Vec3d point) {
        Vec3d originOffset = new Vec3d(
                point.x - this.shapeOrigin.x,
                point.y - this.shapeOrigin.y,
                point.z - this.shapeOrigin.z
        );
        Quaternion inverseRotation = new Quaternion(
                -this.yxzQuat.x,
                -this.yxzQuat.y,
                -this.yxzQuat.z,
                this.yxzQuat.w
        );

        return VectorUtils.rotateVectorWithQuaternion(originOffset, inverseRotation);
    }

    /**
     * Convert a shape space offset into a world space point.
     * The offset is rotated by yxzQuat and translated by shapeOrigin.
     * */
    @NotNull
    protected Vec3d getWorldPointFromShapeOffset(@NotNull Vec3d shapeOffset) {
        Vec3d rotatedOffset = VectorUtils.rotateVectorWithQuaternion(shapeOffset, this.yxzQuat);
        return new Vec3d(
                rotatedOffset.x + this.shapeOrigin.x,
                rotatedOffset.y + this.shapeOrigin.y,
                rotatedOffset.z + this.shapeOrigin.z
        );
    }

    /**
     * Check if a world space point is within the bounds of the shape.
     * */
    public boolean contains(@NotNull Vec3d point) {
        return this.contains(point, false);
    }

    /**
     * Check if a world space point is within the bounds of the shape.
     * If edgeOnly is true, the point must be on the edge for 2d shapes or on the
     * surface for 3d shapes.
     * */
    public boolean contains(@NotNull Vec3d point, boolean edgeOnly) {
        return this.isEquationValueInside(this.getEquationValue(point), edgeOnly);
    }

    /**
     * Create a random world space point within the shape.
     * */
    @NotNull
    public Vec3d randomPoint() {
        return this.randomPoint(false);
    }

    /**
     * Create a random world space point within the shape.
     * If edgeOnly is true, the point will be on the edge for 2d shapes or on the
     * surface for 3d shapes.
     * */
    @NotNull
    public abstract Vec3d randomPoint(boolean edgeOnly);

    /**
     * Rotate the shape using radians.
     * */
    public void rotateShape(double xRad, double yRad, double zRad) {
        this.yxzQuat = QuaternionUtils.createYXZQuaternion(xRad, yRad, zRad);
    }

    /**
     * Rotate the shape using an already-created quaternion.
     * */
    public void rotateShape(@NotNull Quaternion quaternion) {
        this.yxzQuat = new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
        Quaternion.normalise(this.yxzQuat, this.yxzQuat);
    }

    /**
     * Get YXZ quaternion the shape is rotated by.
     * */
    @NotNull
    public Quaternion getYXZQuat() {
        return this.yxzQuat;
    }

    /**
     * Get rotations in rad the shape is rotated by, since not everyone can
     * visualize quaternion rotations in their head (lord kelvin was right
     * when he said that this shit was evil).
     * */
    @NotNull
    public Vec3d getRotations() {
        double x = this.yxzQuat.x;
        double y = this.yxzQuat.y;
        double z = this.yxzQuat.z;
        double w = this.yxzQuat.w;

        double matrix02 = 2 * (x * z + y * w);
        double rotationX = Math.atan2(-2 * (y * z - x * w), 1 - 2 * (x * x + y * y));
        double rotationY = Math.asin(Math.clamp(matrix02, -1, 1));
        double rotationZ = Math.atan2(-2 * (x * y - z * w), 1 - 2 * (y * y + z * z));

        return new Vec3d(rotationX, rotationY, rotationZ);
    }
}
