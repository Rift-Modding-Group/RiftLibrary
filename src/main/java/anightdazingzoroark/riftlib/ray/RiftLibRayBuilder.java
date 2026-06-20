package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.movement.RiftLibRayMovementShape;
import anightdazingzoroark.riftlib.ray.rayShape.movement.RiftLibRayStraightMovementShape;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Builder for ray creation to be passed into RiftLibRay
 */
public final class RiftLibRayBuilder {
    private boolean motionDefined;
    private boolean hasMotion = true;
    private boolean hasImpact;
    private boolean startsWithImpact;

    private boolean segmentsFollowParentDirection;
    private boolean onlyOneSegment;

    @NotNull
    private Supplier<RiftLibRayMovementShape> movementShape = () -> new RiftLibRayStraightMovementShape(1D);
    @NotNull
    private Supplier<RiftLibImpactShape> impactShape = RiftLibRaySphereImpactShape::new;

    private double motionSpeed = 1D;
    private double maxMotionDistance = 16D;

    @NotNull
    private BiFunction<IRayCreator<?>, BlockPos, Boolean> blockBreakCheck = (rayCreator, pos) -> false;

    /**
     * Use this to make the ray only move, no impacts
     * */
    @NotNull
    public RiftLibRayBuilder setMotionOnly() {
        this.setMotionDefinedFlag();
        this.hasMotion = true;
        this.hasImpact = false;
        this.startsWithImpact = false;
        return this;
    }

    /**
     * Use this to make the ray move, then make an impact on hitting an unbreakable block
     * */
    @NotNull
    public RiftLibRayBuilder setMotionThenImpact() {
        this.setMotionDefinedFlag();
        this.hasMotion = true;
        this.hasImpact = true;
        this.startsWithImpact = false;
        return this;
    }

    /**
     * Use this to make the ray only have an impact
     * */
    @NotNull
    public RiftLibRayBuilder setImpactOnly() {
        this.setMotionDefinedFlag();
        this.hasMotion = false;
        this.hasImpact = true;
        this.startsWithImpact = true;
        return this;
    }

    private void setMotionDefinedFlag() {
        if (this.motionDefined) throw new IllegalStateException("This ray's motion is already defined!");
        this.motionDefined = true;
    }

    public boolean getHasMotion() {
        return this.hasMotion;
    }

    public boolean getHasImpact() {
        return this.hasImpact;
    }

    public boolean getStartsWithImpact() {
        return this.startsWithImpact;
    }

    /**
     * Make it so that the segments made by the ray follow the yaw of the creator
     * */
    @NotNull
    public RiftLibRayBuilder setSegmentsFollowParentDirection() {
        this.segmentsFollowParentDirection = true;
        return this;
    }

    public boolean getSegmentsFollowParentDirection() {
        return this.segmentsFollowParentDirection;
    }

    /**
     * Define shape when in motion
     * */
    @NotNull
    public RiftLibRayBuilder setMovementShape(@NotNull Supplier<RiftLibRayMovementShape> movementShape) {
        this.movementShape = movementShape;
        return this;
    }

    @NotNull
    public Supplier<RiftLibRayMovementShape> getMovementShape() {
        return this.movementShape;
    }

    /**
     * Define shape when impacting
     * */
    @NotNull
    public RiftLibRayBuilder setImpactShape(@NotNull Supplier<RiftLibImpactShape> impactShape) {
        this.impactShape = impactShape;
        return this;
    }

    @NotNull
    public Supplier<RiftLibImpactShape> getImpactShape() {
        return this.impactShape;
    }

    /**
     * Define overall speed of the ray
     * */
    @NotNull
    public RiftLibRayBuilder setMotionSpeed(double motionSpeed) {
        this.motionSpeed = motionSpeed;
        return this;
    }

    public double getMotionSpeed() {
        return this.motionSpeed;
    }

    /**
     * Define the overall distance the ray can travel
     * */
    @NotNull
    public RiftLibRayBuilder setMaxMotionDistance(double maxMotionDistance) {
        this.maxMotionDistance = maxMotionDistance;
        return this;
    }

    public double getMaxMotionDistance() {
        return this.maxMotionDistance;
    }

    /**
     * Make it so the ray makes only one segment
     * */
    @NotNull
    public RiftLibRayBuilder setOnlyOneSegment() {
        this.onlyOneSegment = true;
        return this;
    }

    public boolean getOnlyOneSegment() {
        return this.onlyOneSegment;
    }

    /**
     * Create a block break check
     * */
    @NotNull
    public RiftLibRayBuilder setBlockBreakCheck(@NotNull BiFunction<IRayCreator<?>, BlockPos, Boolean> canBreakBlock) {
        this.blockBreakCheck = canBreakBlock;
        return this;
    }

    @NotNull
    public BiFunction<IRayCreator<?>, BlockPos, Boolean> getBlockBreakCheck() {
        return this.blockBreakCheck;
    }

    public boolean isValid() {
        return this.hasMotion || this.hasImpact;
    }
}
