package anightdazingzoroark.riftlib.ray.rayWidth;

import anightdazingzoroark.riftlib.core.util.Axis;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class that holds widths for a ray in motion or upon impact.
 * Takes into account fixed values, 1D ranges, or 3D ranges.
 * */
public class RiftLibRayWidthRange {
    private final double startXWidth, startYWidth, startZWidth; //on 1D widths these are all the same
    private final double endXWidth, endYWidth, endZWidth; //same here
    private final boolean isThreeDim;

    public RiftLibRayWidthRange(double width) {
        this(width, width);
    }

    public RiftLibRayWidthRange(double startWidth, double endWidth) {
        this.startXWidth = startWidth;
        this.startYWidth = startWidth;
        this.startZWidth = startWidth;
        this.endXWidth = endWidth;
        this.endYWidth = endWidth;
        this.endZWidth = endWidth;
        this.isThreeDim = false;
    }

    public RiftLibRayWidthRange(
            double startXWidth, double startYWidth, double startZWidth,
            double endXWidth, double endYWidth, double endZWidth
    ) {
        this.startXWidth = startXWidth;
        this.startYWidth = startYWidth;
        this.startZWidth = startZWidth;
        this.endXWidth = endXWidth;
        this.endYWidth = endYWidth;
        this.endZWidth = endZWidth;
        this.isThreeDim = true;
    }

    public double getStartWidth() {
        if (this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidthRange.getStartWidth() with no args is only supported on 1-dimensional widths!");
        return this.startXWidth;
    }

    public double getStartWidth(@NotNull Axis axis) {
        if (!this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidthRange.getStartWidth() with Axis arg is only supported on 3-dimensional widths!");
        return switch (axis) {
            case X -> this.startXWidth;
            case Y -> this.startYWidth;
            case Z -> this.startZWidth;
        };
    }

    public double getEndWidth() {
        if (this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidthRange.getEndWidth() with no args is only supported on 1-dimensional widths!");
        return this.endXWidth;
    }

    public double getEndWidth(@NotNull Axis axis) {
        if (!this.isThreeDim()) throw new UnsupportedOperationException("RiftLibRayWidthRange.getEndWidth() with Axis arg is only supported on 3-dimensional widths!");
        return switch (axis) {
            case X -> this.endXWidth;
            case Y -> this.endYWidth;
            case Z -> this.endZWidth;
        };
    }

    public boolean isThreeDim() {
        return this.isThreeDim;
    }
}
