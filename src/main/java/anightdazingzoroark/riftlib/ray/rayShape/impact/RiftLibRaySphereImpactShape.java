package anightdazingzoroark.riftlib.ray.rayShape.impact;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.numbers.core.Precision;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class RiftLibRaySphereImpactShape extends RiftLibImpactShape {
    public RiftLibRaySphereImpactShape() {
        super();
    }

    private RiftLibRaySphereImpactShape(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip) {
        super(precision, localClip);
    }

    @Override
    @NotNull
    protected Region<Vector3D> createRegion(double size, @NotNull Precision.DoubleEquivalence precision) {
        return Sphere.from(Vector3D.ZERO, Math.max(MIN_SIZE, size), precision);
    }

    @Override
    protected double maxScale() {
        return 1D;
    }

    @Override
    @NotNull
    protected RiftLibImpactShape withSettings(@NotNull Precision.DoubleEquivalence precision, @NotNull BiPredicate<Vector3D, Double> localClip) {
        return new RiftLibRaySphereImpactShape(precision, localClip);
    }
}
