package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayShape;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidth;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidthRange;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Meant to be created on the client. A ray segment represents a collidable
 * area in motion.
 * */
public class RiftLibRaySegment {
    //AABB of the segment when it is traveling
    @NotNull
    private AxisAlignedBB segmentAABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    //block positions that represent where a segment impact hit, they spread out over time and dissipate.
    //the radius it covers is inversely based on how much along the max ray length did it travel
    //so longer travel coverage = shorter radius and vice versa
    private final LinkedHashSet<BlockPos> segmentImpactPositions = new LinkedHashSet<>();
    private final Set<BlockPos> expiredImpactPositions = new HashSet<>();
    private final List<BlockPos> impactFrontier = new ArrayList<>();

    @NotNull
    public final IRayCreator<?> rayCreator;
    @NotNull
    public final Vec3d initPos;
    @NotNull
    private Vec3d directionVector; //is expected to be a unit vector
    @NotNull
    public final RiftLibRayBuilder builder;
    @NotNull
    private final RiftLibRayShape rayShape;

    @NotNull
    public final RiftLibRayWidth.Mutable currentWidth;
    private double distanceTravelled;
    @NotNull
    private RiftLibRayWidth impactMaxWidth = new RiftLibRayWidth(0D);
    private double impactDepth;
    private int impactLayer;
    @NotNull
    private RiftLibRayWidth.Mutable impactCurrentWidth = new RiftLibRayWidth.Mutable(0D);
    @NotNull
    private RiftLibRayWidth.Mutable impactDecayWidth = new RiftLibRayWidth.Mutable(0D);
    @Nullable
    private BlockPos impactOriginPos;

    private boolean isImpacting;
    private boolean isDead;

    public RiftLibRaySegment(@NotNull IRayCreator<?> rayCreator, @NotNull Vec3d initPos, @NotNull Vec3d directionVector, @NotNull RiftLibRayBuilder builder) {
        this.rayCreator = rayCreator;
        this.initPos = initPos;
        this.directionVector = directionVector.normalize();
        this.builder = builder;
        this.rayShape = builder.getRayShape();

        RiftLibRayWidthRange rayWidthRange = this.builder.getRayWidthRange();
        if (rayWidthRange.isThreeDim()) {
            this.currentWidth = new RiftLibRayWidth.Mutable(
                    rayWidthRange.getStartWidth(Axis.X),
                    rayWidthRange.getStartWidth(Axis.Y),
                    rayWidthRange.getStartWidth(Axis.Z)
            );
        }
        else this.currentWidth = new RiftLibRayWidth.Mutable(rayWidthRange.getStartWidth());

        this.rayShape.onCreateSegment(this);
    }

    /**
     * This updates the segment and is meant to be called from the parent ray.
     * */
    public void onUpdate() {
        //-----do not update if segment is dead-----
        if (this.isDead) return;

        //-----logic when impacting-----
        if (this.isImpacting) this.rayShape.updateImpact(this);
        //-----logic when moving-----
        else {
            //kill when going beyond max ray length
            if (this.distanceTravelled >= this.builder.getRayMaxLength()) {
                this.killSegment();
                return;
            }

            //define how many steps to take and length of each step
            double stepLength = Math.min(this.builder.getRaySpeed(), this.builder.getRayMaxLength() - this.distanceTravelled);
            int steps = Math.max(1, (int)Math.ceil(stepLength / 0.25D));
            double startDistance = this.distanceTravelled;

            //walk along each step
            for (int i = 1; i <= steps; i++) {
                double stepDistance = startDistance + stepLength * i / (double) steps;
                this.moveToDistance(stepDistance);

                ImmutablePair<BlockPos, EnumFacing> impactHit = this.rayShape.findImpactHit(this);
                if (impactHit != null) {
                    this.rayShape.startImpact(this, impactHit.getLeft(), impactHit.getRight());
                    return;
                }
            }
        }
    }

    /**
     * Is necessary for beam type rays where direction vector for each segment changes.
     * */
    public void updateDirectionVector(@NotNull Vec3d directionVector) {
        this.directionVector = directionVector.normalize();
        if (!this.isImpacting) this.moveToDistance(this.distanceTravelled);
    }

    /**
     * Use this to kill this segment and make it just disappear.
     * */
    public void killSegment() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public boolean isImpacting() {
        return this.isImpacting;
    }

    public void setImpacting() {
        this.isImpacting = true;
    }

    public double getDistanceTravelled() {
        return this.distanceTravelled;
    }

    public double getCurrentWidth() {
        if (!this.currentWidth.isThreeDim()) return this.currentWidth.getWidth();
        return this.currentWidth.getMaxWidth();
    }

    @NotNull
    public RiftLibRayWidth getCurrentRayWidth() {
        return this.currentWidth;
    }

    @NotNull
    public Vec3d getDirectionVector() {
        return this.directionVector;
    }

    @NotNull
    public AxisAlignedBB getSegmentAABB() {
        return this.segmentAABB;
    }

    public void setSegmentAABB(@NotNull AxisAlignedBB segmentAABB) {
        this.segmentAABB = segmentAABB;
    }

    public double getImpactMaxRadius() {
        if (!this.impactMaxWidth.isThreeDim()) return this.impactMaxWidth.getWidth();
        return this.impactMaxWidth.getMaxWidth();
    }

    public void setImpactMaxRadius(double value) {
        this.impactMaxWidth = new RiftLibRayWidth(value);
    }

    @NotNull
    public RiftLibRayWidth getImpactMaxWidth() {
        return this.impactMaxWidth;
    }

    public void setImpactMaxWidth(@NotNull RiftLibRayWidth value) {
        this.impactMaxWidth = value;
    }

    public double getImpactDepth() {
        return this.impactDepth;
    }

    public void setImpactDepth(double value) {
        this.impactDepth = value;
    }

    public int getImpactLayer() {
        return this.impactLayer;
    }

    public void setImpactLayer(int impactLayer) {
        this.impactLayer = impactLayer;
    }

    public double getImpactCurrentRadius() {
        if (!this.impactCurrentWidth.isThreeDim()) return this.impactCurrentWidth.getWidth();
        return this.impactCurrentWidth.getMaxWidth();
    }

    public void setImpactCurrentRadius(double impactCurrentRadius) {
        this.impactCurrentWidth = new RiftLibRayWidth.Mutable(impactCurrentRadius);
    }

    @NotNull
    public RiftLibRayWidth.Mutable getImpactCurrentWidth() {
        return this.impactCurrentWidth;
    }

    public void setImpactCurrentWidth(@NotNull RiftLibRayWidth value) {
        this.impactCurrentWidth = value.isThreeDim()
                ? new RiftLibRayWidth.Mutable(value.getWidth(Axis.X), value.getWidth(Axis.Y), value.getWidth(Axis.Z))
                : new RiftLibRayWidth.Mutable(value.getWidth());
    }

    public double getImpactDecayRadius() {
        if (!this.impactDecayWidth.isThreeDim()) return this.impactDecayWidth.getWidth();
        return this.impactDecayWidth.getMaxWidth();
    }

    public void setImpactDecayRadius(double value) {
        this.impactDecayWidth = new RiftLibRayWidth.Mutable(value);
    }

    @NotNull
    public RiftLibRayWidth.Mutable getImpactDecayWidth() {
        return this.impactDecayWidth;
    }

    public void setImpactDecayWidth(@NotNull RiftLibRayWidth value) {
        this.impactDecayWidth = value.isThreeDim()
                ? new RiftLibRayWidth.Mutable(value.getWidth(Axis.X), value.getWidth(Axis.Y), value.getWidth(Axis.Z))
                : new RiftLibRayWidth.Mutable(value.getWidth());
    }

    @Nullable
    public BlockPos getImpactOriginPos() {
        return this.impactOriginPos;
    }

    public void setImpactOriginPos(@Nullable BlockPos pos) {
        this.impactOriginPos = pos;
    }

    public boolean hasImpactPositions() {
        return !this.segmentImpactPositions.isEmpty();
    }

    public LinkedHashSet<BlockPos> getSegmentImpactPositions() {
        return this.segmentImpactPositions;
    }

    @NotNull
    public Set<BlockPos> getExpiredImpactPositions() {
        return this.expiredImpactPositions;
    }

    @NotNull
    public List<BlockPos> getImpactFrontier() {
        return this.impactFrontier;
    }

    public List<AxisAlignedBB> getSegmentAABBList() {
        return List.of(this.segmentAABB);
    }

    private void moveToDistance(double distance) {
        this.distanceTravelled = distance;

        RiftLibRayWidthRange rayWidthRange = this.builder.getRayWidthRange();
        double maxLength = this.builder.getRayMaxLength();

        if (rayWidthRange.isThreeDim()) {
            if (maxLength <= 0D) {
                this.currentWidth.setWidth(Axis.X, rayWidthRange.getEndWidth(Axis.X));
                this.currentWidth.setWidth(Axis.Y, rayWidthRange.getEndWidth(Axis.Y));
                this.currentWidth.setWidth(Axis.Z, rayWidthRange.getEndWidth(Axis.Z));
            }
            else {
                this.currentWidth.setWidth(
                        Axis.X,
                        MathUtils.slopeResult(distance, true, 0D, maxLength, rayWidthRange.getStartWidth(Axis.X), rayWidthRange.getEndWidth(Axis.X))
                );
                this.currentWidth.setWidth(
                        Axis.Y,
                        MathUtils.slopeResult(distance, true, 0D, maxLength, rayWidthRange.getStartWidth(Axis.Y), rayWidthRange.getEndWidth(Axis.Y))
                );
                this.currentWidth.setWidth(
                        Axis.Z,
                        MathUtils.slopeResult(distance, true, 0D, maxLength, rayWidthRange.getStartWidth(Axis.Z), rayWidthRange.getEndWidth(Axis.Z))
                );
            }
        }
        else {
            if (maxLength <= 0D) this.currentWidth.setWidth(rayWidthRange.getEndWidth());
            else this.currentWidth.setWidth(MathUtils.slopeResult(distance, true, 0D, maxLength, rayWidthRange.getStartWidth(), rayWidthRange.getEndWidth()));
        }

        this.rayShape.updateMovingShape(this);
    }

    /**
     * Provide decay for impact results
     * */
    public void decayImpactPositions() {
        if (this.segmentImpactPositions.isEmpty()) return;

        double decaySpeed = Math.max(0D, this.builder.getRaySpeed());
        if (this.impactDecayWidth.isThreeDim()) {
            this.impactDecayWidth.setWidth(Axis.X, this.impactDecayWidth.getWidth(Axis.X) + decaySpeed);
            this.impactDecayWidth.setWidth(Axis.Y, this.impactDecayWidth.getWidth(Axis.Y) + decaySpeed);
            this.impactDecayWidth.setWidth(Axis.Z, this.impactDecayWidth.getWidth(Axis.Z) + decaySpeed);
        }
        else this.impactDecayWidth.setWidth(this.impactDecayWidth.getWidth() + decaySpeed);
        if (this.getImpactDecayRadius() <= 0D) return;

        Iterator<BlockPos> it = this.segmentImpactPositions.iterator();

        while (it.hasNext()) {
            BlockPos impactPos = it.next();
            //test if given block pos is within impact decay front
            if (!this.rayShape.isWithinImpactDecayFront(this, impactPos)) continue;

            it.remove();
            this.expiredImpactPositions.add(impactPos);
        }
    }

    @NotNull
    public Set<BlockPos> getBlockPositionsInCurrentShape() {
        return this.rayShape.getBlockPositionsInCurrentShape(this);
    }

    public boolean intersectsEntity(@NotNull Entity entity) {
        return this.rayShape.intersectsEntity(this, entity);
    }

    //-----debug lines are all dealt with here. they're for client use and for helping out with visualizing ray segments when debugging-----
    public record DebugLine(@NotNull Vec3d start, @NotNull Vec3d end) {}

    @NotNull
    public List<DebugLine> getDebugGridLines() {
        return this.rayShape.getDebugGridLines(this);
    }
}
