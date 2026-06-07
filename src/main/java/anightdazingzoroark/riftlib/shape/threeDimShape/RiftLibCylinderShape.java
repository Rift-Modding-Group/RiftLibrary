package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class RiftLibCylinderShape extends RiftLibThreeDimShape {
    protected double radius, height;

    public RiftLibCylinderShape(@NotNull Vec3d shapeOrigin, double radius, double height) {
        super(shapeOrigin);
        this.radius = radius;
        this.height = height;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double radialDistance = Math.sqrt(shapeOffset.x * shapeOffset.x + shapeOffset.z * shapeOffset.z);
        double yDistance = Math.abs(shapeOffset.y);

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
                this.random.nextDouble() * this.height - this.height / 2D,
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
            double yOffset = surfacePosition < capArea ? -this.height / 2D : this.height / 2D;

            return new Vec3d(
                    Math.cos(angle) * radialDistance,
                    yOffset,
                    Math.sin(angle) * radialDistance
            );
        }

        double angle = this.random.nextDouble() * Math.PI * 2D;
        return new Vec3d(
                Math.cos(angle) * this.radius,
                this.random.nextDouble() * this.height - this.height / 2D,
                Math.sin(angle) * this.radius
        );
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

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }
}
