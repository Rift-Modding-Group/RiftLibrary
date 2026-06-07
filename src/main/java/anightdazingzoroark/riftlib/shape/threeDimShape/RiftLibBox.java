package anightdazingzoroark.riftlib.shape.threeDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * "Box" is more concise than "Rectangular cuboid", now take that dumbass mathematicians.
 * */
public class RiftLibBox extends RiftLibThreeDimShape {
    private final double xLength, yLength, zLength;

    //presumes use of cube
    public RiftLibBox(@NotNull Vec3d shapeOrigin, double sideLength) {
        this(shapeOrigin, sideLength, sideLength, sideLength);
    }

    public RiftLibBox(@NotNull Vec3d shapeOrigin, double xLength, double yLength, double zLength) {
        super(shapeOrigin);
        this.xLength = xLength;
        this.yLength = yLength;
        this.zLength = zLength;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double halfLength = this.xLength / 2.0;
        double halfWidth = this.yLength / 2.0;
        double halfHeight = this.zLength / 2.0;
        double xDistance = Math.abs(shapeOffset.x);
        double yDistance = Math.abs(shapeOffset.y);
        double zDistance = Math.abs(shapeOffset.z);
        return Math.max(Math.max(xDistance / halfLength, yDistance / halfHeight), zDistance / halfWidth);
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
        double halfLength = this.xLength / 2.0;
        double halfWidth = this.yLength / 2.0;
        double halfHeight = this.zLength / 2.0;
        if (surfaceOnly) {
            double lengthWidthArea = this.xLength * this.yLength;
            double lengthHeightArea = this.xLength * this.zLength;
            double widthHeightArea = this.yLength * this.zLength;
            double surfacePosition = this.random.nextDouble() * (lengthWidthArea * 2.0 + lengthHeightArea * 2.0 + widthHeightArea * 2.0);
            double xOffset = (this.random.nextDouble() - 0.5) * this.xLength;
            double yOffset = (this.random.nextDouble() - 0.5) * this.zLength;
            double zOffset = (this.random.nextDouble() - 0.5) * this.yLength;

            if (surfacePosition < lengthWidthArea) {
                return new Vec3d(xOffset, -halfHeight, zOffset);
            }
            else if (surfacePosition < lengthWidthArea * 2.0) {
                return new Vec3d(xOffset, halfHeight, zOffset);
            }
            else if (surfacePosition < lengthWidthArea * 2.0 + lengthHeightArea) {
                return new Vec3d(xOffset, yOffset, -halfWidth);
            }
            else if (surfacePosition < lengthWidthArea * 2.0 + lengthHeightArea * 2.0) {
                return new Vec3d(xOffset, yOffset, halfWidth);
            }
            else if (surfacePosition < lengthWidthArea * 2.0 + lengthHeightArea * 2.0 + widthHeightArea) {
                return new Vec3d(-halfLength, yOffset, zOffset);
            }
            else return new Vec3d(halfLength, yOffset, zOffset);
        }

        return new Vec3d(
                (this.random.nextDouble() - 0.5) * this.xLength,
                (this.random.nextDouble() - 0.5) * this.zLength,
                (this.random.nextDouble() - 0.5) * this.yLength
        );
    }

    @Override
    public double getVolume() {
        return this.xLength * this.yLength * this.zLength * this.getCutoffFraction();
    }

    @Override
    public double getSurfaceArea() {
        double sideOneArea = this.xLength * this.yLength;
        double sideTwoArea = this.xLength * this.zLength;
        double sideThreeArea = this.yLength * this.zLength;
        return (sideOneArea * 2 + sideTwoArea * 2 + sideThreeArea * 2) * this.getCutoffFraction();
    }
}
