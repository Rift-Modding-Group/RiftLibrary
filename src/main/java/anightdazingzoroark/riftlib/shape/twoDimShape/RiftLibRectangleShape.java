package anightdazingzoroark.riftlib.shape.twoDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class RiftLibRectangleShape extends RiftLibTwoDimShape {
    private final double xWidth;
    private final double zWidth;

    public RiftLibRectangleShape(@NotNull Vec3d shapeOrigin, double sideWidth) {
        this(shapeOrigin, sideWidth, sideWidth);
    }

    public RiftLibRectangleShape(@NotNull Vec3d shapeOrigin, double xWidth, double zWidth) {
        super(shapeOrigin);
        this.xWidth = xWidth;
        this.zWidth = zWidth;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double halfWidth = this.xWidth / 2D;
        double halfHeight = this.zWidth / 2D;
        double xDistance = Math.abs(shapeOffset.x);
        double zDistance = Math.abs(shapeOffset.z);
        return Math.max(xDistance / halfWidth, zDistance / halfHeight);
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean edgeOnly) {
        double halfWidth = this.xWidth / 2D;
        double halfHeight = this.zWidth / 2D;
        Vec3d shapeOffset;

        if (edgeOnly) {
            double perimeterPosition = this.random.nextDouble() * this.getPerimeter();

            if (perimeterPosition < this.xWidth) {
                shapeOffset = new Vec3d(-halfWidth + perimeterPosition, 0, -halfHeight);
            }
            else if (perimeterPosition < this.xWidth + this.zWidth) {
                shapeOffset = new Vec3d(halfWidth, 0, -halfHeight + perimeterPosition - this.xWidth);
            }
            else if (perimeterPosition < this.xWidth * 2D + this.zWidth) {
                shapeOffset = new Vec3d(halfWidth - (perimeterPosition - this.xWidth - this.zWidth), 0, halfHeight);
            }
            else shapeOffset = new Vec3d(-halfWidth, 0, halfHeight - (perimeterPosition - this.xWidth * 2D - this.zWidth));
        }
        else {
            shapeOffset = new Vec3d(
                    this.random.nextDouble() * this.xWidth,
                    0,
                    this.random.nextDouble() * this.zWidth
            );
        }

        return this.getWorldPointFromShapeOffset(shapeOffset);
    }

    @Override
    public double getArea() {
        return this.xWidth * this.zWidth;
    }

    @Override
    public double getPerimeter() {
        return this.xWidth * 2 + this.zWidth * 2;
    }
}
