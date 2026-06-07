package anightdazingzoroark.riftlib.shape.twoDimShape;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class RiftLibEllipseShape extends RiftLibTwoDimShape {
    private final double xRadius;
    private final double zRadius;

    //presumes creation of a good ol circle
    public RiftLibEllipseShape(@NotNull Vec3d shapeOrigin, double radius) {
        this(shapeOrigin, radius, radius);
    }

    public RiftLibEllipseShape(@NotNull Vec3d shapeOrigin, double xRadius, double zRadius) {
        super(shapeOrigin);
        this.xRadius = xRadius;
        this.zRadius = zRadius;
    }

    @Override
    protected double getEquationValue(@NotNull Vec3d point) {
        Vec3d shapeOffset = this.getUnrotatedShapeOffset(point);
        double xDistance = shapeOffset.x;
        double zDistance = shapeOffset.z;
        double xTerm = xDistance * xDistance / (this.xRadius * this.xRadius);
        double zTerm = zDistance * zDistance / (this.zRadius * this.zRadius);
        return xTerm + zTerm;
    }

    @Override
    @NotNull
    public Vec3d randomPoint(boolean edgeOnly) {
        double angle = this.random.nextDouble() * Math.PI * 2D;
        double radiusScale = edgeOnly ? 1D : Math.sqrt(this.random.nextDouble());
        Vec3d shapeOffset = new Vec3d(
                Math.cos(angle) * this.xRadius * radiusScale,
                0,
                Math.sin(angle) * this.zRadius * radiusScale
        );

        return this.getWorldPointFromShapeOffset(shapeOffset);
    }

    @Override
    public double getArea() {
        return Math.PI * this.xRadius * this.zRadius;
    }

    @Override
    public double getPerimeter() {
        double a = this.xRadius;
        double b = this.zRadius;
        double h = ((a - b) * (a - b)) / ((a + b) * (a + b));
        return Math.PI * (a + b) * (1.0 + (3.0 * h) / (10.0 + Math.sqrt(4.0 - 3.0 * h)));
    }
}
