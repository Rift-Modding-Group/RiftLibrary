package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.ray.rayShape.movement.RiftLibRayMovementShape;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Meant to be simultaneously created on the server and the client. This defines "rays",
 * which are essentially vectors that affect any blocks or entities that cross them.
 */
public class RiftLibRay {
    @NotNull
    public final IRayCreator<?> rayCreator;
    @NotNull
    public final String rayName;
    @NotNull
    public final AnimatedLocator parentLocator;
    @NotNull
    public final RiftLibRayBuilder builder;

    //the origin of the ray. is nullable so that upon initialization there
    //will not be a giant puddle below the user
    @Nullable
    private Vec3d originPos;

    //last valid locator pose, retained while existing segments finish after the ray ends
    @Nullable
    private RayPose rayPose;

    //all moving and impacting segments in the ray
    @NotNull
    private final List<RiftLibRaySegment> raySegmentList = new ArrayList<>();

    //how many segments have ever been created; used only for the onlyOneSegment flag
    private int segmentsCreated;

    //shape for ray in motion
    @NotNull
    private final RiftLibRayMovementShape movementShape;

    //flag to signal the start of the end of the ray
    private boolean isEnded;
    //flag to signal removal of the ray
    private boolean isDead;

    public RiftLibRay(
            @NotNull IRayCreator<?> rayCreator,
            @NotNull String rayName,
            @NotNull AnimatedLocator parentLocator,
            @NotNull RiftLibRayBuilder builder
    ) {
        this.rayCreator = rayCreator;
        this.rayName = rayName;
        this.parentLocator = parentLocator;
        this.builder = builder;
        this.movementShape = builder.getMovementShape().get();
    }

    /**
     * This updates the ray and is meant to be called on both sides.
     */
    public void onUpdate() {
        //-----kill the ray and its segments if the ray creator died, or if the locator disappeared-----
        if (!this.rayCreator.getRayCreator().isEntityAlive() || !this.parentLocator.isValid()) {
            this.killRay();
        }

        //-----do not update if ray is dead-----
        if (this.isDead) return;

        //-----define the ray pose when not fading out-----
        if (!this.isEnded) {
            this.rayPose = this.currentPose();
            this.originPos = this.rayPose.origin();
        }

        //-----define sets of entities and block positions to send to server-----
        Set<Entity> hitEntities = new HashSet<>();
        Set<BlockPos> hitBlocks = new LinkedHashSet<>();

        //-----create ray segments-----
        if (this.rayPose != null) {
            if (!this.isEnded && this.builder.getStartsWithImpact() && this.segmentsCreated == 0) {
                this.raySegmentList.add(new RiftLibRaySegment(
                        this.rayCreator,
                        this.rayPose.origin(),
                        this.rayPose.direction(),
                        this.rayPose.up(),
                        new BlockPos(this.rayPose.origin()),
                        null,
                        this.builder
                ));
                this.segmentsCreated++;
            }

            for (RiftLibRaySegment raySegment : this.raySegmentList) {
                raySegment.tick(this.rayPose, hitBlocks, hitEntities);
            }

            if (!this.isEnded && this.builder.getHasMotion() && this.canCreateMoreSegments()) {
                for (RiftLibRayMovementShape.SegmentSeed seed : this.movementShape.createSegments(this.rayPose, this.builder)) {
                    if (!this.canCreateMoreSegments()) break;
                    this.raySegmentList.add(new RiftLibRaySegment(
                            this.rayCreator,
                            seed.center(),
                            seed.direction(),
                            seed.length(),
                            this.builder
                    ));
                    this.segmentsCreated++;
                }
            }
        }

        //-----send ray information to the ray creator-----
        if (!this.rayCreator.getRayCreator().world.isRemote
                && this.originPos != null
                && (!hitBlocks.isEmpty() || !hitEntities.isEmpty())) {
            this.rayCreator.applyRaySegments(
                    this.rayName,
                    new BlockPos(this.originPos),
                    new RayHitResult(hitEntities, hitBlocks)
            );
        }

        //-----remove segments after callers had a tick to consume their final state-----
        this.raySegmentList.removeIf(RiftLibRaySegment::isDead);
        if (this.raySegmentList.isEmpty()
                && (this.builder.getStartsWithImpact() || this.builder.getOnlyOneSegment())) {
            this.endRay();
        }

        //-----kill ray when ended and theres no more segments to update-----
        if (this.isEnded && this.raySegmentList.isEmpty()) this.killRay();
    }

    private boolean canCreateMoreSegments() {
        //only legal maximums are 1 or infinite
        return !this.builder.getOnlyOneSegment() || this.segmentsCreated == 0;
    }

    /**
     * Use this to end this ray to make it fade out.
     */
    public void endRay() {
        this.isEnded = true;
    }

    /**
     * Use this to kill this ray and make it just disappear.
     */
    public void killRay() {
        for (RiftLibRaySegment raySegment : this.raySegmentList) raySegment.killSegment();
        this.raySegmentList.clear();
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    /**
     * Define the current ray pose using ray creator and locator position
     * */
    @NotNull
    private RayPose currentPose() {
        //---common stuff---
        //determine entity yaw and turn into quaternion
        double normalYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYawHead);
        double riddenYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYaw);
        double finalYawRadians = this.rayCreator.getRayCreator().isBeingRidden() ? riddenYawRadians : normalYawRadians;

        //---for origin---
        //set initial entity offset from center, in model space ofc
        Vec3d animatedLocatorPos = this.parentLocator.getModelSpacePosition();
        Vec3d posVec = new Vec3d(
                -animatedLocatorPos.x / 16f * this.rayCreator.rayCreatorScale(),
                animatedLocatorPos.y / 16f * this.rayCreator.rayCreatorScale(),
                -animatedLocatorPos.z / 16f * this.rayCreator.rayCreatorScale()
        );

        //rotate the locator position only by the entity yaw; the locator's own rotation affects direction, not origin
        posVec = VectorUtils.rotateVectorWithQuaternion(
                posVec,
                QuaternionUtils.createXYZQuaternion(0D, finalYawRadians, 0D)
        );

        //position to entity pos
        posVec = new Vec3d(
                posVec.x + this.rayCreator.getRayCreator().posX,
                posVec.y + this.rayCreator.getRayCreator().posY,
                posVec.z + this.rayCreator.getRayCreator().posZ
        );

        //---for direction vector and up vector---
        //rotate the model axes by the locator, then apply the same X/Z conversion
        //used by the locator position before rotating into entity world space
        Quaternion locatorQuaternion = this.parentLocator.getModelSpaceYXZQuaternion();
        Quaternion.normalise(locatorQuaternion, locatorQuaternion);

        Vec3d modelForward = VectorUtils.rotateVectorWithQuaternion(
                new Vec3d(0D, 0D, -1D),
                locatorQuaternion
        ).normalize();
        Vec3d modelUp = VectorUtils.rotateVectorWithQuaternion(
                new Vec3d(0D, 1D, 0D),
                locatorQuaternion
        ).normalize();
        Vec3d forwardVec = new Vec3d(-modelForward.x, modelForward.y, -modelForward.z);
        Vec3d upVec = new Vec3d(-modelUp.x, modelUp.y, -modelUp.z);

        Quaternion entityYaw = QuaternionUtils.createYXZQuaternion(0D, finalYawRadians, 0D);
        forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, entityYaw).normalize();
        upVec = VectorUtils.rotateVectorWithQuaternion(upVec, entityYaw).normalize();

        return new RayPose(posVec, forwardVec, upVec);
    }

    /**
     * Debug grid lines are the visualization of the ray and are visible when hitbox view is on.
     * They are meant for client use only.
     */
    public void forEachDebugLine(float partialTicks, @NotNull BiConsumer<Vec3d, Vec3d> lineConsumer) {
        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            if (raySegment.isDead()) continue;
            raySegment.forEachDebugLine(partialTicks, lineConsumer);
        }
    }

    /**
     * This ray result is immutable and is sent to the IRayCreator on the server only.
     */
    public record RayHitResult(Set<Entity> hitEntities, Set<BlockPos> hitBlockPositions) {}

    /**
     * World-space locator pose for this tick.
     */
    public record RayPose(@NotNull Vec3d origin, @NotNull Vec3d direction, @NotNull Vec3d up) {
        public RayPose {
            direction = direction.normalize();
            up = up.normalize();
            if (Math.abs(direction.dotProduct(up)) > 0.999D) {
                up = Math.abs(direction.y) > 0.999D
                        ? new Vec3d(1D, 0D, 0D)
                        : new Vec3d(0D, 1D, 0D);
            }
        }

        @NotNull
        public Vec3d right() {
            return this.direction.crossProduct(this.up).normalize();
        }

        @NotNull
        public Vec3d offset(double rightAmount, double upAmount, double forwardAmount) {
            return this.origin.add(this.right().scale(rightAmount))
                    .add(this.up.scale(upAmount))
                    .add(this.direction.scale(forwardAmount));
        }
    }
}
