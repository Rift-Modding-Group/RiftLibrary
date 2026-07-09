package anightdazingzoroark.riftlib.ray.rayShape.motion;

import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class RiftLibRayConeMotionShape extends RiftLibRayMotionShape {
    private final double horizontalSpread;
    private final double verticalSpread;
    private final int rings;
    private final int samplesPerRing;
    private final boolean anchoredAtTop;
    private final boolean anchoredAtBottom;

    public RiftLibRayConeMotionShape(
            double segmentLength,
            double horizontalSpread,
            double verticalSpread,
            int rings,
            int samplesPerRing,
            boolean anchoredAtTop,
            boolean anchoredAtBottom
    ) {
        super(segmentLength);
        this.horizontalSpread = horizontalSpread;
        this.verticalSpread = verticalSpread;
        this.rings = Math.max(1, rings);
        this.samplesPerRing = Math.max(1, samplesPerRing);
        this.anchoredAtTop = anchoredAtTop;
        this.anchoredAtBottom = anchoredAtBottom;
    }

    @Override
    @NotNull
    public List<SegmentSeed> createSegments(@NotNull RiftLibRay.RayPose pose, @NotNull RiftLibRayBuilder builder) {
        List<SegmentSeed> result = new ArrayList<>();
        result.add(new SegmentSeed(pose.origin(), pose.direction(), this.segmentLength()));

        for (int ring = 1; ring <= this.rings; ring++) {
            double ringRatio = ring / (double)this.rings;
            double ringX = this.horizontalSpread * ringRatio;
            double ringY = this.verticalSpread * ringRatio;

            for (int i = 0; i < this.samplesPerRing; i++) {
                double angle = Math.PI * 2D * i / this.samplesPerRing;
                double localX = Math.cos(angle) * ringX;
                double localY = Math.sin(angle) * ringY;

                if (this.anchoredAtTop) localY -= ringY;
                else if (this.anchoredAtBottom) localY += ringY;

                Vec3d direction = pose.direction()
                        .add(pose.right().scale(localX))
                        .add(pose.up().scale(localY))
                        .normalize();
                result.add(new SegmentSeed(pose.origin(), direction, this.segmentLength()));
            }
        }

        return result;
    }
}
