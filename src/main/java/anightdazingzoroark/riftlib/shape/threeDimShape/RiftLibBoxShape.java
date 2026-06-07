package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * "Box" is more concise than "Rectangular cuboid", now take that dumbass mathematicians.
 * */
public class RiftLibBoxShape extends RiftLibThreeDimShape {
    protected double xLength, yLength, zLength;

    //presumes use of cube
    public RiftLibBoxShape(@NotNull Vec3d shapeOrigin, double sideLength) {
        this(shapeOrigin, sideLength, sideLength, sideLength);
    }

    public RiftLibBoxShape(@NotNull Vec3d shapeOrigin, double xLength, double yLength, double zLength) {
        super(shapeOrigin);
        this.xLength = xLength;
        this.yLength = yLength;
        this.zLength = zLength;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double halfXLength = this.xLength / 2D;
        double halfYLength = this.yLength / 2D;
        double halfZLength = this.zLength / 2D;
        double xDistance = Math.abs(shapeOffset.x);
        double yDistance = Math.abs(shapeOffset.y);
        double zDistance = Math.abs(shapeOffset.z);
        return Math.max(Math.max(xDistance / halfXLength, yDistance / halfYLength), zDistance / halfZLength);
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean surfaceOnly) {
        for (int i = 0; i < CUTOFF_RANDOM_ATTEMPTS; i++) {
            Vec3d shapeOffset = this.createRandomShapeOffset(surfaceOnly);
            if (this.isShapeOffsetWithinCutoffs(shapeOffset)) return this.getWorldPointFromShapeOffset(shapeOffset);
        }

        throw new IllegalStateException("Unable to generate a random box point within the current cutoffs.");
    }

    @NotNull
    private Vec3d createRandomShapeOffset(boolean surfaceOnly) {
        double halfXLength = this.xLength / 2D;
        double halfYLength = this.yLength / 2D;
        double halfZLength = this.zLength / 2D;
        if (surfaceOnly) {
            double lengthWidthArea = this.xLength * this.zLength;
            double lengthHeightArea = this.xLength * this.yLength;
            double widthHeightArea = this.zLength * this.yLength;
            double surfacePosition = this.random.nextDouble() * (lengthWidthArea * 2D + lengthHeightArea * 2D + widthHeightArea * 2D);
            double xOffset = this.random.nextDouble() * this.xLength;
            double yOffset = this.random.nextDouble() * this.yLength;
            double zOffset = this.random.nextDouble() * this.zLength;

            if (surfacePosition < lengthWidthArea) {
                return new Vec3d(xOffset, -halfYLength, zOffset);
            }
            else if (surfacePosition < lengthWidthArea * 2D) {
                return new Vec3d(xOffset, halfYLength, zOffset);
            }
            else if (surfacePosition < lengthWidthArea * 2D + lengthHeightArea) {
                return new Vec3d(xOffset, yOffset, -halfZLength);
            }
            else if (surfacePosition < lengthWidthArea * 2D + lengthHeightArea * 2D) {
                return new Vec3d(xOffset, yOffset, halfZLength);
            }
            else if (surfacePosition < lengthWidthArea * 2D + lengthHeightArea * 2D + widthHeightArea) {
                return new Vec3d(-halfXLength, yOffset, zOffset);
            }
            else return new Vec3d(halfXLength, yOffset, zOffset);
        }

        return new Vec3d(
                this.random.nextDouble() * this.xLength,
                this.random.nextDouble() * this.yLength,
                this.random.nextDouble() * this.zLength
        );
    }

    @Override
    public double getVolume() {
        return this.xLength * this.yLength * this.zLength * this.getCutoffFraction();
    }

    @Override
    public double getSurfaceArea() {
        double sideOneArea = this.xLength * this.zLength;
        double sideTwoArea = this.xLength * this.yLength;
        double sideThreeArea = this.zLength * this.yLength;
        return (sideOneArea * 2 + sideTwoArea * 2 + sideThreeArea * 2) * this.getCutoffFraction();
    }

    /**
     * Mutable variant of this class.
     * */
    public static class Mutable extends RiftLibBoxShape {
        public Mutable(@NotNull Vec3d shapeOrigin, double sideLength) {
            super(shapeOrigin, sideLength);
        }

        public Mutable(@NotNull Vec3d shapeOrigin, double xLength, double yLength, double zLength) {
            super(shapeOrigin, xLength, yLength, zLength);
        }

        public void setSideLength(double sideLength) {
            this.xLength = sideLength;
            this.yLength = sideLength;
            this.zLength = sideLength;
        }

        public void setLengths(double xLength, double yLength, double zLength) {
            this.xLength = xLength;
            this.yLength = yLength;
            this.zLength = zLength;
        }
    }
}
