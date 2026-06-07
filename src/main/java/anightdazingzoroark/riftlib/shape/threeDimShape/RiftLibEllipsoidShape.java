package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class RiftLibEllipsoidShape extends RiftLibThreeDimShape {
    protected double xRadius, yRadius, zRadius;

    //presumes use of sphere
    public RiftLibEllipsoidShape(@NotNull Vec3d shapeOrigin, double radius) {
        this(shapeOrigin, radius, radius, radius);
    }

    public RiftLibEllipsoidShape(@NotNull Vec3d shapeOrigin, double xRadius, double yRadius, double zRadius) {
        super(shapeOrigin);
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double xTerm = shapeOffset.x * shapeOffset.x / (this.xRadius * this.xRadius);
        double yTerm = shapeOffset.y * shapeOffset.y / (this.yRadius * this.yRadius);
        double zTerm = shapeOffset.z * shapeOffset.z / (this.zRadius * this.zRadius);
        return xTerm + yTerm + zTerm;
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean surfaceOnly) {
        for (int i = 0; i < CUTOFF_RANDOM_ATTEMPTS; i++) {
            Vec3d shapeOffset = this.createRandomShapeOffset(surfaceOnly);
            if (this.isShapeOffsetWithinCutoffs(shapeOffset)) return this.getWorldPointFromShapeOffset(shapeOffset);
        }

        throw new IllegalStateException("Unable to generate a random ellipsoid point within the current cutoffs.");
    }

    @NotNull
    private Vec3d createRandomShapeOffset(boolean surfaceOnly) {
        double yDirection = this.random.nextDouble() * 2.0 - 1.0;
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double horizontalScale = Math.sqrt(1.0 - yDirection * yDirection);
        double radiusScale = surfaceOnly ? 1.0 : Math.cbrt(this.random.nextDouble());
        return new Vec3d(
                Math.cos(angle) * horizontalScale * this.xRadius * radiusScale,
                yDirection * this.yRadius * radiusScale,
                Math.sin(angle) * horizontalScale * this.zRadius * radiusScale
        );
    }

    @Override
    public double getVolume() {
        return 4D / 3D * Math.PI * this.xRadius * this.yRadius * this.zRadius * this.getCutoffFraction();
    }

    //thank you knud thomsen, very cool!
    @Override
    public double getSurfaceArea() {
        double power = 1.6075;
        double xy = Math.pow(this.xRadius * this.yRadius, power);
        double xz = Math.pow(this.xRadius * this.zRadius, power);
        double yz = Math.pow(this.yRadius * this.zRadius, power);
        return 4D * Math.PI * Math.pow((xy + xz + yz) / 3D, 1D / power) * this.getCutoffFraction();
    }

    /**
     * Mutable variant of this class.
     * */
    public static class Mutable extends RiftLibEllipsoidShape {
        public Mutable(@NotNull Vec3d shapeOrigin, double radius) {
            super(shapeOrigin, radius);
        }

        public Mutable(@NotNull Vec3d shapeOrigin, double xRadius, double yRadius, double zRadius) {
            super(shapeOrigin, xRadius, yRadius, zRadius);
        }

        /**
         * If the ellipsoid is presumed to be a sphere, use this to update
         * its total radius.
         * */
        public void setRadius(double radius) {
            this.xRadius = radius;
            this.yRadius = radius;
            this.zRadius = radius;
        }

        /**
         * If the ellipsoid is presumed to be a, well, ellipsoid, use this to
         * update its radiuses.
         * */
        public void setRadius(double xRadius, double yRadius, double zRadius) {
            this.xRadius = xRadius;
            this.yRadius = yRadius;
            this.zRadius = zRadius;
        }
    }
}
