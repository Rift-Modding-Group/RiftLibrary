package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * In cones, the origin is always considered to be the point where all the slanted lines meet.
 * In other words... the top.
 * */
public class RiftLibConeShape extends RiftLibThreeDimShape {
    protected double baseRadius, height;

    public RiftLibConeShape(@NotNull Vec3d shapeOrigin, double baseRadius, double height) {
        super(shapeOrigin, false);
        this.baseRadius = baseRadius;
        this.height = height;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double heightProgress = -shapeOffset.y / this.height;
        double radialDistance = Math.sqrt(shapeOffset.x * shapeOffset.x + shapeOffset.z * shapeOffset.z);

        if (heightProgress < 0D) return 1D - heightProgress;
        if (heightProgress <= EQUATION_EPSILON) return radialDistance <= EQUATION_EPSILON ? 1D : 1D + radialDistance / this.baseRadius;

        double radiusAtY = this.baseRadius * heightProgress;
        return Math.max(radialDistance / radiusAtY, heightProgress);
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean surfaceOnly) {
        for (int i = 0; i < CUTOFF_RANDOM_ATTEMPTS; i++) {
            Vec3d shapeOffset = this.createRandomShapeOffset(surfaceOnly);
            if (this.isShapeOffsetWithinCutoffs(shapeOffset)) return this.getWorldPointFromShapeOffset(shapeOffset);
        }

        throw new IllegalStateException("Unable to generate a random cone point within the current cutoffs.");
    }

    @NotNull
    private Vec3d createRandomShapeOffset(boolean surfaceOnly) {
        if (surfaceOnly) return this.createRandomSurfaceOffset();
        return this.createRandomVolumeOffset();
    }

    @NotNull
    private Vec3d createRandomVolumeOffset() {
        double heightProgress = Math.cbrt(this.random.nextDouble());
        double angle = this.random.nextDouble() * Math.PI * 2D;
        double radialDistance = this.baseRadius * heightProgress * Math.sqrt(this.random.nextDouble());

        return new Vec3d(
                Math.cos(angle) * radialDistance,
                -this.height * heightProgress,
                Math.sin(angle) * radialDistance
        );
    }

    @NotNull
    private Vec3d createRandomSurfaceOffset() {
        double baseArea = Math.PI * this.baseRadius * this.baseRadius;
        double sideArea = Math.PI * this.baseRadius * this.getSlantHeight();

        if (this.random.nextDouble() * (baseArea + sideArea) < baseArea) {
            double angle = this.random.nextDouble() * Math.PI * 2D;
            double radialDistance = this.baseRadius * Math.sqrt(this.random.nextDouble());
            return new Vec3d(
                    Math.cos(angle) * radialDistance,
                    -this.height,
                    Math.sin(angle) * radialDistance
            );
        }

        double heightProgress = Math.sqrt(this.random.nextDouble());
        double angle = this.random.nextDouble() * Math.PI * 2D;
        double radialDistance = this.baseRadius * heightProgress;
        return new Vec3d(
                Math.cos(angle) * radialDistance,
                -this.height * heightProgress,
                Math.sin(angle) * radialDistance
        );
    }

    @Override
    public double getVolume() {
        return Math.PI * this.baseRadius * this.baseRadius * this.height / 3D * this.getCutoffFraction();
    }

    @Override
    public double getSurfaceArea() {
        return Math.PI * this.baseRadius * (this.baseRadius + this.getSlantHeight()) * this.getCutoffFraction();
    }

    private double getSlantHeight() {
        return Math.sqrt(this.baseRadius * this.baseRadius + this.height * this.height);
    }

    /**
     * Mutable variant of this class.
     * */
    public static class Mutable extends RiftLibConeShape {
        public Mutable(@NotNull Vec3d shapeOrigin, double baseRadius, double height) {
            super(shapeOrigin, baseRadius, height);
        }

        public void setBaseRadius(double baseRadius) {
            this.baseRadius = baseRadius;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }
}
