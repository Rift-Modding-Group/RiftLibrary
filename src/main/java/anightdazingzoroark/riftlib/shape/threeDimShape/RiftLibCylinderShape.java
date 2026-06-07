package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class RiftLibCylinderShape extends RiftLibThreeDimShape {
    protected double radius, height;

    public RiftLibCylinderShape(@NotNull Vec3d shapeOrigin, double radius, double height) {
        this(shapeOrigin, true, radius, height);
    }

    public RiftLibCylinderShape(@NotNull Vec3d shapeOrigin, boolean originIsCenter, double radius, double height) {
        super(shapeOrigin, originIsCenter);
        this.radius = radius;
        this.height = height;
    }

    public double getRadius() {
        return this.radius;
    }

    public double getHeight() {
        return this.height;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double radialDistance = Math.sqrt(shapeOffset.x * shapeOffset.x + shapeOffset.z * shapeOffset.z);
        double yDistance = Math.abs(shapeOffset.y - this.getYCenterOffset());

        return Math.max(radialDistance / this.radius, yDistance / (this.height / 2D));
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean surfaceOnly) {
        for (int i = 0; i < CUTOFF_RANDOM_ATTEMPTS; i++) {
            Vec3d shapeOffset = this.createRandomShapeOffset(surfaceOnly);
            if (this.isShapeOffsetWithinCutoffs(shapeOffset)) return this.getWorldPointFromShapeOffset(shapeOffset);
        }

        throw new IllegalStateException("Unable to generate a random cylinder point within the current cutoffs.");
    }

    @NotNull
    private Vec3d createRandomShapeOffset(boolean surfaceOnly) {
        if (surfaceOnly) return this.createRandomSurfaceOffset();
        return this.createRandomVolumeOffset();
    }

    @NotNull
    private Vec3d createRandomVolumeOffset() {
        double angle = this.random.nextDouble() * Math.PI * 2D;
        double radialDistance = this.radius * Math.sqrt(this.random.nextDouble());

        return new Vec3d(
                Math.cos(angle) * radialDistance,
                this.randomYOffset(),
                Math.sin(angle) * radialDistance
        );
    }

    @NotNull
    private Vec3d createRandomSurfaceOffset() {
        double capArea = Math.PI * this.radius * this.radius;
        double sideArea = 2D * Math.PI * this.radius * this.height;
        double surfacePosition = this.random.nextDouble() * (capArea * 2D + sideArea);

        if (surfacePosition < capArea * 2D) {
            double angle = this.random.nextDouble() * Math.PI * 2D;
            double radialDistance = this.radius * Math.sqrt(this.random.nextDouble());
            double yOffset = surfacePosition < capArea ? this.getYMinOffset() : this.getYMaxOffset();

            return new Vec3d(
                    Math.cos(angle) * radialDistance,
                    yOffset,
                    Math.sin(angle) * radialDistance
            );
        }

        double angle = this.random.nextDouble() * Math.PI * 2D;
        return new Vec3d(
                Math.cos(angle) * this.radius,
                this.randomYOffset(),
                Math.sin(angle) * this.radius
        );
    }

    private double randomYOffset() {
        return this.random.nextDouble() * this.height + this.getYMinOffset();
    }

    private double getYCenterOffset() {
        if (this.originIsCenter) return 0D;
        return -this.height / 2D;
    }

    private double getYMinOffset() {
        if (this.originIsCenter) return -this.height / 2D;
        return -this.height;
    }

    private double getYMaxOffset() {
        if (this.originIsCenter) return this.height / 2D;
        return 0D;
    }

    @Override
    public double getVolume() {
        return Math.PI * this.radius * this.radius * this.height * this.getCutoffFraction();
    }

    @Override
    public double getSurfaceArea() {
        return 2D * Math.PI * this.radius * (this.radius + this.height) * this.getCutoffFraction();
    }

    /**
     * Mutable variant of this class.
     * */
    public static class Mutable extends RiftLibCylinderShape {
        public Mutable(@NotNull Vec3d shapeOrigin, double radius, double height) {
            super(shapeOrigin, radius, height);
        }

        public Mutable(@NotNull Vec3d shapeOrigin, boolean originIsCenter, double radius, double height) {
            super(shapeOrigin, originIsCenter, radius, height);
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }
}
