package anightdazingzoroark.riftlib.ray.rayShape.impact;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class RiftLibRayCustomImpactShape extends RiftLibImpactShape {
    @NotNull
    private final BiFunction<Double, Precision.DoubleEquivalence, Region<Vector3D>> regionFactory;
    private final double maxScale;

    public RiftLibRayCustomImpactShape(
            @NotNull BiFunction<Double, Precision.DoubleEquivalence, Region<Vector3D>> regionFactory,
            double maxScale
    ) {
        this(
                Precision.doubleEquivalenceOfEpsilon(1.0E-6D),
                (point, size) -> true,
                regionFactory,
                maxScale
        );
    }

    private RiftLibRayCustomImpactShape(
            @NotNull Precision.DoubleEquivalence precision,
            @NotNull BiPredicate<Vector3D, Double> localClip,
            @NotNull BiFunction<Double, Precision.DoubleEquivalence, Region<Vector3D>> regionFactory,
            double maxScale
    ) {
        super(precision, localClip);
        this.regionFactory = regionFactory;
        this.maxScale = Math.max(MIN_SIZE, Math.abs(maxScale));
    }

    @Override
    @NotNull
    protected Region<Vector3D> createRegion(double size, @NotNull Precision.DoubleEquivalence precision) {
        return this.regionFactory.apply(Math.max(MIN_SIZE, size), precision);
    }

    @Override
    protected double maxScale() {
        return this.maxScale;
    }

    @Override
    @NotNull
    protected RiftLibImpactShape withSettings(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip) {
        return new RiftLibRayCustomImpactShape(precision, localClip, this.regionFactory, this.maxScale);
    }
}
