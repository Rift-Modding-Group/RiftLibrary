package anightdazingzoroark.riftlib.ray.rayShape.movement;

import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Owns how moving ray segments are created.
 */
public abstract class RiftLibRayMovementShape {
    private final double segmentLength;

    protected RiftLibRayMovementShape(double segmentLength) {
        this.segmentLength = Math.max(0.001D, segmentLength);
    }

    public double segmentLength() {
        return this.segmentLength;
    }

    @NotNull
    public abstract List<SegmentSeed> createSegments(@NotNull RiftLibRay.RayPose pose, @NotNull RiftLibRayBuilder builder);

    public record SegmentSeed(@NotNull Vec3d center, @NotNull Vec3d direction, double length) {}
}
