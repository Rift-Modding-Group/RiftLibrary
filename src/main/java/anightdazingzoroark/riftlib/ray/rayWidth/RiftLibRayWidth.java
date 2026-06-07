package anightdazingzoroark.riftlib.ray.rayWidth;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.util.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for holding ray widths.
 * Takes into account 1D widths and 3d widths
 * */
public class RiftLibRayWidth {
    protected double xWidth, yWidth, zWidth;
    private final boolean isThreeDim;

    public RiftLibRayWidth(double width) {
        this(width, width, width, false);
    }

    public RiftLibRayWidth(double xWidth, double yWidth, double zWidth) {
        this(xWidth, yWidth, zWidth, true);
    }

    protected RiftLibRayWidth(double xWidth, double yWidth, double zWidth, boolean isThreeDim) {
        this.xWidth = xWidth;
        this.yWidth = yWidth;
        this.zWidth = zWidth;
        this.isThreeDim = isThreeDim;
    }

    public double getWidth() {
        if (!this.isThreeDim()) return this.xWidth;
        throw new UnsupportedOperationException("RiftLibRayWidth.getWidth() with no args is only supported on 1-dimensional widths!");
    }

    public double getWidth(@NotNull Axis axis) {
        if (!this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidth.getWidth() with Axis arg is only supported on 3-dimensional widths!");
        return switch (axis) {
            case X -> this.xWidth;
            case Y -> this.yWidth;
            case Z -> this.zWidth;
        };
    }

    public boolean isThreeDim() {
        return this.isThreeDim;
    }

    public double getMaxWidth() {
        if (!this.isThreeDim()) return this.getWidth();
        return MathUtils.max(this.xWidth, this.yWidth, this.zWidth);
    }

    public static class Mutable extends RiftLibRayWidth {
        public Mutable(double width) {
            super(width, width, width, false);
        }

        public Mutable(double xWidth, double yWidth, double zWidth) {
            super(xWidth, yWidth, zWidth, true);
        }

        public void setWidth(double width) {
            if (this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidth.setWidth() with only 1 arg is only supported on 1-dimensional widths!");
            this.xWidth = width;
            this.yWidth = width;
            this.zWidth = width;
        }

        public void setWidth(@NotNull Axis axis, double width) {
            if (!this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidth.setWidth() with Axis and width args is only supported on 3-dimensional widths!");
            switch (axis) {
                case X -> this.xWidth = width;
                case Y -> this.yWidth = width;
                case Z -> this.zWidth = width;
            }
        }
    }
}
