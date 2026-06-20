package anightdazingzoroark.riftlib.ray.rayShape.impact;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class RiftLibRayEllipsoidImpactShape extends RiftLibImpactShape {
    private final double xScale;
    private final double yScale;
    private final double zScale;

    public RiftLibRayEllipsoidImpactShape(double xScale, double yScale, double zScale) {
        this(
                Precision.doubleEquivalenceOfEpsilon(1.0E-6D),
                (point, size) -> true,
                xScale,
                yScale,
                zScale
        );
    }

    private RiftLibRayEllipsoidImpactShape(
            @NotNull Precision.DoubleEquivalence precision,
            @NotNull BiPredicate<Vector3D, Double> localClip,
            double xScale,
            double yScale,
            double zScale
    ) {
        super(precision, localClip);
        this.xScale = Math.max(MIN_SIZE, Math.abs(xScale));
        this.yScale = Math.max(MIN_SIZE, Math.abs(yScale));
        this.zScale = Math.max(MIN_SIZE, Math.abs(zScale));
    }

    @Override
    @NotNull
    protected Region<Vector3D> createRegion(double size, @NotNull Precision.DoubleEquivalence precision) {
        return new EllipsoidRegion(
                Math.max(MIN_SIZE, size * this.xScale),
                Math.max(MIN_SIZE, size * this.yScale),
                Math.max(MIN_SIZE, size * this.zScale),
                precision
        );
    }

    @Override
    protected double maxScale() {
        return Math.max(this.xScale, Math.max(this.yScale, this.zScale));
    }

    @Override
    @NotNull
    protected RiftLibImpactShape withSettings(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip) {
        return new RiftLibRayEllipsoidImpactShape(precision, localClip, this.xScale, this.yScale, this.zScale);
    }

    private record EllipsoidRegion(double xRadius, double yRadius, double zRadius, @NotNull Precision.DoubleEquivalence precision) implements Region<Vector3D> {
        @Override
        public boolean isFull() {
                return false;
            }

        @Override
        public boolean isEmpty() {
                return false;
            }

        @Override
        public double getSize() {
                return 4D / 3D * Math.PI * this.xRadius * this.yRadius * this.zRadius;
            }

        @Override
        public double getBoundarySize() {
            double power = 1.6075D;
            double xy = Math.pow(this.xRadius * this.yRadius, power);
            double xz = Math.pow(this.xRadius * this.zRadius, power);
            double yz = Math.pow(this.yRadius * this.zRadius, power);
            return 4D * Math.PI * Math.pow((xy + xz + yz) / 3D, 1D / power);
        }

        @Override
        public Vector3D getCentroid() {
                return Vector3D.ZERO;
            }

        @Override
        public RegionLocation classify(Vector3D point) {
            double equation = this.equation(point);
            if (this.precision.eq(equation, 1D)) return RegionLocation.BOUNDARY;
            return equation < 1D ? RegionLocation.INSIDE : RegionLocation.OUTSIDE;
        }

        @Override
        public Vector3D project(Vector3D point) {
            double norm = Math.sqrt(this.equation(point));
            if (this.precision.eqZero(norm)) return Vector3D.of(this.xRadius, 0D, 0D);
            return Vector3D.of(point.getX() / norm, point.getY() / norm, point.getZ() / norm);
        }

        private double equation(@NotNull Vector3D point) {
            double xTerm = point.getX() * point.getX() / (this.xRadius * this.xRadius);
            double yTerm = point.getY() * point.getY() / (this.yRadius * this.yRadius);
            double zTerm = point.getZ() * point.getZ() / (this.zRadius * this.zRadius);
            return xTerm + yTerm + zTerm;
        }
    }
}
