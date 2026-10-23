package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.keyframe.BoneAnimation;
import anightdazingzoroark.riftlib.core.keyframe.VectorKeyFrameList;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.core.util.MathUtil;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples geometry without advancing controllers or running animation events.
 * */
public class AnimationPoseSampler {
    @NotNull
    public static Map<String, BoneSnapshot> sample(@NotNull Animation animation, @NotNull AbstractAnimationData<?, ?> data, double tick) {
        Map<String, BoneSnapshot> poses = new HashMap<>();
        data.getBoneSnapshotCollection().forEach((name, entry) -> poses.put(name, new BoneSnapshot(entry.getLeft().getInitialSnapshot())));
        for (BoneAnimation channel : animation.boneAnimations) {
            BoneSnapshot pose = poses.get(channel.boneName);
            if (pose == null) continue;
            if (!channel.rotationKeyFrames.isEmpty()) pose.getRotation().add(sampleVector(channel.rotationKeyFrames, data, tick));
            if (!channel.positionKeyFrames.isEmpty()) pose.getPosition().add(sampleVector(channel.positionKeyFrames, data, tick));
            if (!channel.scaleKeyFrames.isEmpty()) {
                Vector3f scale = sampleVector(channel.scaleKeyFrames, data, tick);
                pose.getScale().set(pose.getScale().x * scale.x, pose.getScale().y * scale.y, pose.getScale().z * scale.z);
            }
        }
        return poses;
    }

    @NotNull
    public static Map<String, BoneSnapshot> capture(@NotNull AbstractAnimationData<?, ?> data) {
        Map<String, BoneSnapshot> poses = new HashMap<>();
        data.getBoneSnapshotCollection().forEach((name, entry) -> poses.put(name, new BoneSnapshot(entry.getRight())));
        return poses;
    }

    @NotNull
    public static Map<String, BoneSnapshot> interpolate(@NotNull Map<String, BoneSnapshot> before, @NotNull Map<String, BoneSnapshot> after, float progress) {
        Map<String, BoneSnapshot> poses = new HashMap<>();
        after.forEach((name, current) -> {
            BoneSnapshot pose = new BoneSnapshot(current);
            BoneSnapshot previous = before.get(name);
            if (previous != null) {
                interpolateVector(previous.getPosition(), current.getPosition(), pose.getPosition(), progress);
                interpolateVector(previous.getRotation(), current.getRotation(), pose.getRotation(), progress);
                interpolateVector(previous.getScale(), current.getScale(), pose.getScale(), progress);
            }
            poses.put(name, pose);
        });
        return poses;
    }

    @NotNull
    private static Vector3f sampleVector(@NotNull VectorKeyFrameList frames, @NotNull AbstractAnimationData<?, ?> data, double tick) {
        return new Vector3f(
                MathUtil.lerpValues(frames.getAnimationPointAtTick(data, tick, Axis.X), EasingType.NONE, null),
                MathUtil.lerpValues(frames.getAnimationPointAtTick(data, tick, Axis.Y), EasingType.NONE, null),
                MathUtil.lerpValues(frames.getAnimationPointAtTick(data, tick, Axis.Z), EasingType.NONE, null)
        );
    }

    private static void interpolateVector(@NotNull Vector3f before, @NotNull Vector3f after, @NotNull Vector3f result, float progress) {
        result.set(Interpolations.lerp(before.x, after.x, progress), Interpolations.lerp(before.y, after.y, progress), Interpolations.lerp(before.z, after.z, progress));
    }
}
