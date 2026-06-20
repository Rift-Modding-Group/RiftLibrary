package anightdazingzoroark.riftlib.ray.rayShape.impact;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Common base for impact shapes.
 */
public abstract class RiftLibImpactShape {
    protected static final double MIN_SIZE = 0.001D;

    @NotNull
    private final Precision.DoubleEquivalence precision;
    @NotNull
    private final BiPredicate<Vector3D, Double> localClip;

    protected RiftLibImpactShape() {
        this(Precision.doubleEquivalenceOfEpsilon(1.0E-6D), (point, size) -> true);
    }

    protected RiftLibImpactShape(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip) {
        this.precision = precision;
        this.localClip = localClip;
    }

    @NotNull
    public RiftLibImpactShape clip(@NotNull Predicate<Vector3D> clip) {
        return this.withSettings(this.precision, this.localClip.and((point, size) -> clip.test(point)));
    }

    @NotNull
    public RiftLibImpactShape topOnly() {
        return this.clip(point -> point.getY() >= 0D);
    }

    @NotNull
    public RiftLibImpactShape bottomOnly() {
        return this.clip(point -> point.getY() <= 0D);
    }

    @NotNull
    public RiftLibImpactShape topPercent(double percentFromTop) {
        double percent = Math.clamp(percentFromTop, 0D, 1D);
        return this.withSettings(
                this.precision,
                this.localClip.and((point, size) -> point.getY() >= size * (1D - percent))
        );
    }

    @NotNull
    public Region<Vector3D> createRegion(double size) {
        return this.createRegion(Math.max(MIN_SIZE, size), this.precision);
    }

    public boolean contains(@NotNull Region<Vector3D> region, @NotNull Vector3D localPoint, double size) {
        return region.contains(localPoint) && this.localClip.test(localPoint, size);
    }

    public int blockExtent(double size) {
        return Math.max(1, (int)Math.ceil(Math.abs(size) * this.maxScale()));
    }

    @NotNull
    protected abstract Region<Vector3D> createRegion(double size, @NotNull Precision.DoubleEquivalence precision);

    protected abstract double maxScale();

    @NotNull
    protected abstract RiftLibImpactShape withSettings(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip);
}
