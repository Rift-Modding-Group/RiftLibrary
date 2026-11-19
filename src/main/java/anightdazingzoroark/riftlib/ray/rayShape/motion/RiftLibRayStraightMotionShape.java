package anightdazingzoroark.riftlib.ray.rayShape.motion;

import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RiftLibRayStraightMotionShape extends RiftLibRayMotionShape {
    private final double sourceWidth;

    public RiftLibRayStraightMotionShape(double segmentLength) {
        this(segmentLength, 0D);
    }

    public RiftLibRayStraightMotionShape(double segmentLength, double sourceWidth) {
        super(segmentLength);
        this.sourceWidth = Math.max(0D, sourceWidth);
    }

    @Override
    @NotNull
    public List<SegmentSeed> createSegments(@NotNull RiftLibRay.RayPose pose, @NotNull RiftLibRayBuilder builder) {
        SegmentSeed center = new SegmentSeed(pose.origin(), pose.direction(), this.segmentLength());
        if (this.sourceWidth == 0D) return List.of(center);

        List<SegmentSeed> result = new ArrayList<>();
        result.add(center);
        Vec3d right = pose.right();
        Vec3d up = right.crossProduct(pose.direction()).normalize();
        int rings = (int) Math.ceil(this.sourceWidth);
        for (int ring = 1; ring <= rings; ring++) {
            double radius = this.sourceWidth * 0.5D * (ring / (double) rings);
            int samples = Math.max(4, 4 * (int) Math.ceil(Math.PI * radius));
            for (int sample = 0; sample < samples; sample++) {
                double angle = Math.TAU * sample / samples;
                result.add(new SegmentSeed(
                        pose.origin().add(right.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius)),
                        pose.direction(),
                        this.segmentLength()
                ));
            }
        }
        return result;
    }
}
