package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidth;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibEllipsoidShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibThreeDimShape;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Impact-only ellipsoid ray shape. Its impact dimensions come directly from
 * the current width on each axis.
 * */
public class RiftLibRayEllipsoidImpactShape extends RiftLibRayImpactShape {
    private static final double MIN_RADIUS = 0.001D;

    private final boolean upperOnly;

    public RiftLibRayEllipsoidImpactShape(boolean upperOnly) {
        this.upperOnly = upperOnly;
    }

    @Override
    @NotNull
    protected RiftLibThreeDimShape createImpactShape(@NotNull RiftLibRaySegment segment, @NotNull Vec3d origin, @NotNull RiftLibRayWidth width, double depth) {
        double[] radii = this.getRadii(width);
        RiftLibEllipsoidShape.Mutable ellipsoidShape = new RiftLibEllipsoidShape.Mutable(origin, radii[0], radii[1], radii[2]);
        if (this.upperOnly) ellipsoidShape.addCutoff(RiftLibThreeDimShape.ThreeDimCutoff.NEG_Y);
        return ellipsoidShape;
    }

    @Override
    @NotNull
    protected List<RiftLibRaySegment.DebugLine> createDebugGridLines(@NotNull RiftLibRaySegment segment) {
        return this.createImpactSphereGridLines(segment, this.upperOnly);
    }

    private double[] getRadii(@NotNull RiftLibRayWidth width) {
        if (!width.isThreeDim()) {
            double radius = Math.max(MIN_RADIUS, width.getWidth());
            return new double[]{radius, radius, radius};
        }

        return new double[]{
                Math.max(MIN_RADIUS, width.getWidth(Axis.X)),
                Math.max(MIN_RADIUS, width.getWidth(Axis.Y)),
                Math.max(MIN_RADIUS, width.getWidth(Axis.Z))
        };
    }
}
