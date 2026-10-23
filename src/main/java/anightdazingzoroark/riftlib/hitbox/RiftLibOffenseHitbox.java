package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import java.util.ArrayList;
import java.util.Map;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This optional hitbox deals with attacks performed by IMultiHitboxUser instances
 * if they ever wanna deal with soulslike combat hitboxes
 * */
public class RiftLibOffenseHitbox<T extends IMultiHitboxUser<?>> implements IHitbox<T> {
    @NotNull
    private final T parent;
    @NotNull
    private final AnimatedBoundingBox boundingBox;
    private Set<Integer> alreadyHitEntities;
    private Map<String, BoneSnapshot> previousPose;
    private Vec3d previousPosition;
    private float previousYaw;
    private float previousScale;
    private boolean isDead;

    public RiftLibOffenseHitbox(@NotNull T parent, @NotNull AnimatedBoundingBox boundingBox) {
        this(parent, boundingBox, new HashSet<>());
    }

    public RiftLibOffenseHitbox(@NotNull T parent, @NotNull AnimatedBoundingBox boundingBox, @NotNull Set<Integer> alreadyHitEntities) {
        this.parent = parent;
        this.boundingBox = boundingBox;
        this.alreadyHitEntities = alreadyHitEntities;
    }

    /**
     * Only ever update on server
     * */
    public void onUpdate() {
        //-----dont update further when dead-----
        if (this.isDead) return;
        if (!this.getParent().getMultiHitboxUser().isEntityAlive()) {
            this.kill();
            return;
        }

        // Sample the posed volume between server ticks instead of skipping a fast jaw/tail sweep.
        Entity parentEntity = this.getParent().getMultiHitboxUser();
        Map<String, BoneSnapshot> pose = AnimationPoseSampler.capture(this.getParent().getMultiHitboxUser().getAnimationData());
        Vec3d position = parentEntity.getPositionVector();
        float yaw = this.getParent().getMultiHitboxUser().getAnimationData().getModelYaw();
        float scale = this.getParent().getMultiHitboxUser().getAnimationData().getScale();
        List<AxisAlignedBB> samples = new ArrayList<>();
        for (int step = 0; step <= 4; step++) {
            float progress = step / 4f;
            Map<String, BoneSnapshot> samplePose = pose;
            Vec3d samplePosition = position;
            float sampleYaw = yaw;
            float sampleScale = scale;
            if (this.previousPose != null) {
                samplePose = AnimationPoseSampler.interpolate(this.previousPose, pose, progress);
                samplePosition = new Vec3d(
                        Interpolations.lerp(this.previousPosition.x, position.x, progress),
                        Interpolations.lerp(this.previousPosition.y, position.y, progress),
                        Interpolations.lerp(this.previousPosition.z, position.z, progress));
                sampleYaw = Interpolations.lerpYaw(this.previousYaw, yaw, progress);
                sampleScale = Interpolations.lerp(this.previousScale, scale, progress);
            }
            samples.add(this.boundingBox.getWorldSpaceBounds(sampleScale, sampleYaw, samplePosition, samplePose));
        }
        AxisAlignedBB hitboxAABB = samples.getFirst();
        for (AxisAlignedBB sample : samples) hitboxAABB = hitboxAABB.union(sample);

        List<Entity> candidates = this.getParent().getWorld().getEntitiesWithinAABB(Entity.class, hitboxAABB, new Predicate<Entity>() {
            @Override
            public boolean apply(Entity input) {
                return input != parentEntity && this.isHitboxOfParent(input);
            }

            private boolean isHitboxOfParent(Entity entity) {
                if (!(entity instanceof MultiPartEntityPart multiPartEntityPart)) return true;
                return multiPartEntityPart.parent != parentEntity;
            }
        });

        //-----apply damage to hit entities-----
        for (Entity target : candidates) {
            if (samples.stream().noneMatch(sample -> sample.intersects(target.getEntityBoundingBox()))) continue;
            Entity victim = target;
            if (target instanceof MultiPartEntityPart part && part.parent instanceof Entity entity) victim = entity;
            if (!this.alreadyHitEntities.add(victim.getEntityId())) continue;
            this.getParent().attackEntityWithHitbox(target);
        }
        this.previousPose = pose;
        this.previousPosition = position;
        this.previousYaw = yaw;
        this.previousScale = scale;
    }

    public void kill() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    @Override
    @NonNull
    public T getParent() {
        return this.parent;
    }

    @Override
    @NonNull
    public AnimatedBoundingBox getBoundingBox() {
        return this.boundingBox;
    }
}
