package anightdazingzoroark.riftlib.ray.rayShape.impact;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.core.Precision;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class RiftLibRayBoxImpactShape extends RiftLibImpactShape {
    private final double xScale;
    private final double yScale;
    private final double zScale;

    public RiftLibRayBoxImpactShape(double xScale, double yScale, double zScale) {
        this(
                Precision.doubleEquivalenceOfEpsilon(1.0E-6D),
                (point, size) -> true,
                xScale, yScale, zScale
        );
    }

    private RiftLibRayBoxImpactShape(
            @NotNull Precision.DoubleEquivalence precision,
            @NotNull BiPredicate<Vector3D, Double> localClip,
            double xScale, double yScale, double zScale
    ) {
        super(precision, localClip);
        this.xScale = Math.max(MIN_SIZE, Math.abs(xScale));
        this.yScale = Math.max(MIN_SIZE, Math.abs(yScale));
        this.zScale = Math.max(MIN_SIZE, Math.abs(zScale));
    }

    @Override
    @NotNull
    protected Region<Vector3D> createRegion(double size, @NotNull Precision.DoubleEquivalence precision) {
        double x = Math.max(MIN_SIZE, size * this.xScale);
        double y = Math.max(MIN_SIZE, size * this.yScale);
        double z = Math.max(MIN_SIZE, size * this.zScale);
        return Parallelepiped.axisAligned(
                Vector3D.of(-x / 2D, -y / 2D, -z / 2D),
                Vector3D.of(x / 2D, y / 2D, z / 2D),
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
        return new RiftLibRayBoxImpactShape(precision, localClip, this.xScale, this.yScale, this.zScale);
    }
}
