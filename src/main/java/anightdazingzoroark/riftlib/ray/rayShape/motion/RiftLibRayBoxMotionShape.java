package anightdazingzoroark.riftlib.ray.rayShape.motion;

import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class RiftLibRayBoxMotionShape extends RiftLibRayMotionShape {
    private final double width;
    private final boolean anchoredAtTop;
    private final boolean anchoredAtBottom;

    public RiftLibRayBoxMotionShape(
            double segmentLength,
            double width,
            boolean anchoredAtTop,
            boolean anchoredAtBottom
    ) {
        super(segmentLength);
        this.width = Math.max(0D, width);
        this.anchoredAtTop = anchoredAtTop;
        this.anchoredAtBottom = anchoredAtBottom;
    }

    @Override
    @NotNull
    public List<SegmentSeed> createSegments(@NotNull RiftLibRay.RayPose pose, @NotNull RiftLibRayBuilder builder) {
        List<SegmentSeed> result = new ArrayList<>();
        int widthSamples = (int) Math.max(1, Math.ceil(this.width));

        for (int ix = 0; ix < widthSamples; ix++) {
            double xRatio = widthSamples == 1 ? 0.5D : ix / (double)(widthSamples - 1);
            double localX = -this.width * 0.5D + this.width * xRatio;
            double localY = 0.5D + ((!this.anchoredAtTop && !this.anchoredAtBottom) ? 0.5D : 0);

            result.add(new SegmentSeed(
                    pose.offset(localX, localY, 0D),
                    pose.direction(),
                    this.segmentLength()
            ));
        }

        return result;
    }
}
