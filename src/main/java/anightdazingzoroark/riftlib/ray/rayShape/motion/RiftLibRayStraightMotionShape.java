package anightdazingzoroark.riftlib.ray.rayShape.motion;

import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RiftLibRayStraightMotionShape extends RiftLibRayMotionShape {
    public RiftLibRayStraightMotionShape(double segmentLength) {
        super(segmentLength);
    }

    @Override
    @NotNull
    public List<SegmentSeed> createSegments(@NotNull RiftLibRay.RayPose pose, @NotNull RiftLibRayBuilder builder) {
        return List.of(new SegmentSeed(pose.origin(), pose.direction(), this.segmentLength()));
    }
}
