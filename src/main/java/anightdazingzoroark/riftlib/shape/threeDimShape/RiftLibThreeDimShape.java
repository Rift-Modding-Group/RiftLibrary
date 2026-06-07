package anightdazingzoroark.riftlib.shape.threeDimShape;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.shape.RiftLibShape;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class RiftLibThreeDimShape extends RiftLibShape {
    protected static final int CUTOFF_RANDOM_ATTEMPTS = 10000;

    //if false, origin is on the top
    protected final boolean originIsCenter;
    //list of cutoff points this shape will consider
    protected final List<ThreeDimCutoff> cutoffs = new ArrayList<>();

    public RiftLibThreeDimShape(@NotNull Vec3d shapeOrigin, boolean originIsCenter) {
        super(shapeOrigin);
        this.originIsCenter = originIsCenter;
    }

    @Override
    public boolean contains(@NotNull Vec3d point, boolean surfaceOnly) {
        return super.contains(point, surfaceOnly) && this.isWithinCutoffs(point);
    }

    /**
     * Check if the provided point is within the cutoffs of the shape.
     * */
    protected boolean isWithinCutoffs(@NotNull Vec3d point) {
        return this.isShapeOffsetWithinCutoffs(this.getUnrotatedShapeOffset(point));
    }

    protected boolean isShapeOffsetWithinCutoffs(@NotNull Vec3d shapeOffset) {
        for (ThreeDimCutoff cutoff : this.cutoffs) {
            if (!cutoff.acceptOffsetCondition.apply(shapeOffset)) return false;
        }

        return true;
    }

    protected double getCutoffFraction() {
        double fraction = 1D;
        for (Axis axis : Axis.values()) {
            long axisCutoffs = this.cutoffs.stream().filter(cutoff -> cutoff.axis == axis).count();
            if (axisCutoffs > 1) return 0D;
            if (axisCutoffs == 0) continue;
            if (!this.originIsCenter && axis == Axis.Y) {
                if (this.cutoffs.contains(ThreeDimCutoff.NEG_Y)) return 0D;
                continue;
            }

            fraction *= 0.5D;
        }

        return fraction;
    }

    /**
     * Compute volume of 3d shape
     * */
    public abstract double getVolume();

    /**
     * Compute surface area of 3d shape
     * */
    public abstract double getSurfaceArea();

    public boolean getOriginIsCenter() {
        return this.originIsCenter;
    }

    /**
     * Add a cutoff to the shape.
     * */
    public void addCutoff(@NotNull ThreeDimCutoff threeDimCutoff) {
        if (this.cutoffs.contains(threeDimCutoff)) return;
        this.cutoffs.add(threeDimCutoff);
    }

    /**
     * A cutoff point removes the matching side of a RiftLibThreeDimShape from the origin.
     * Multiple of them can be put in a 3d shape.
     * */
    public enum ThreeDimCutoff {
        POS_X(Axis.X, shapeOffset -> shapeOffset.x <= EQUATION_EPSILON),
        NEG_X(Axis.X, shapeOffset -> shapeOffset.x >= -EQUATION_EPSILON),
        POS_Y(Axis.Y, shapeOffset -> shapeOffset.y <= EQUATION_EPSILON),
        NEG_Y(Axis.Y, shapeOffset -> shapeOffset.y >= -EQUATION_EPSILON),
        POS_Z(Axis.Z, shapeOffset -> shapeOffset.z <= EQUATION_EPSILON),
        NEG_Z(Axis.Z, shapeOffset -> shapeOffset.z >= -EQUATION_EPSILON);

        @NotNull
        public final Axis axis;
        @NotNull
        public final Function<Vec3d, Boolean> acceptOffsetCondition;

        ThreeDimCutoff(@NotNull Axis axis, @NotNull Function<Vec3d, Boolean> acceptOffsetCondition) {
            this.axis = axis;
            this.acceptOffsetCondition = acceptOffsetCondition;
        }
    }
}
