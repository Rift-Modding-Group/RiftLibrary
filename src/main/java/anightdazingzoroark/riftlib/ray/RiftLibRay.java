package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayBeamShape;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayMovingShape;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayShape;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayUpperSphereImpactShape;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;
import java.util.function.Function;

/**
 * Meant to be simultaneously created on the server and the client. This defines "rays",
 * which are essentially vectors that affect any blocks or entities that cross
 * them.
 * */
public class RiftLibRay {
    @NotNull
    private final IRayCreator<?> rayCreator;
    @NotNull
    public final String rayName;
    @NotNull
    public final AnimatedLocator parentLocator;
    @NotNull
    private final RiftLibRayShape rayShape;
    private final double maxRayLength;
    private final double @NotNull [] rayWidthRange;
    //in blocks per tick, defines the speed in which the segments travel
    private final double raySpeed;
    //if true, the ray will spread upon hitting a block
    private final boolean spreadOnHitBlock;
    //create only one segment
    private final boolean onlyOneSegment;
    //define if the ray can break this block
    @NotNull
    private final Function<BlockPos, Boolean> breakBlockCondition;

    //the origin of the ray. is nullable so that upon initialization there
    //will not be a giant puddle below the user
    @Nullable
    private Vec3d originPos;
    //the direction the ray must travel in
    @NotNull
    private Vec3d directionVector = new Vec3d(0, 0, -1);

    //all the segments in a ray
    private final List<RiftLibRaySegment> raySegmentList = new ArrayList<>();

    //flag to signal the start of the end of the ray
    private boolean isEnded;
    //flag to signal removal of the ray
    private boolean isDead;

    public RiftLibRay(
            @NotNull IRayCreator<?> rayCreator, @NotNull String rayName, @NotNull AnimatedLocator parentLocator, @NotNull RayType rayType,
            double maxRayLength, double @NotNull [] rayWidthRange, double raySpeed, boolean spreadOnHitBlock, boolean onlyOneSegment,
            @NotNull Function<BlockPos, Boolean> breakBlockCondition
    ) {
        this.rayCreator = rayCreator;
        this.rayName = rayName;
        this.parentLocator = parentLocator;
        this.rayShape = rayType.getShape();
        this.maxRayLength = maxRayLength;
        this.rayWidthRange = rayWidthRange;
        this.raySpeed = raySpeed;
        this.spreadOnHitBlock = spreadOnHitBlock;
        this.onlyOneSegment = onlyOneSegment;
        this.breakBlockCondition = breakBlockCondition;
    }

    /**
     * This updates the ray and is meant to be called on both sides.
     * */
    public void onUpdate() {
        //-----kill the ray and its segments if the ray creator died for some reason-----
        if (!this.rayCreator.getRayCreator().isEntityAlive()) {
            for (RiftLibRaySegment raySegment : this.raySegmentList) raySegment.killSegment();
            this.killRay();
        }

        //-----do not update if ray is dead-----
        if (this.isDead) return;

        //-----define the ray origin pos and direction vector when not fading out-----
        if (!this.isEnded) {
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
            posVec = VectorUtils.rotateVectorWithQuaternion(posVec, QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0));

            //position to entity pos
            posVec = new Vec3d(
                    posVec.x + this.rayCreator.getRayCreator().posX,
                    posVec.y + this.rayCreator.getRayCreator().posY,
                    posVec.z + this.rayCreator.getRayCreator().posZ
            );

            //set final pos vec
            this.originPos = posVec;

            //---for direction vector---
            //correct quaternion for animated locator to work in world space
            Quaternion correction = QuaternionUtils.createYXZQuaternion(Math.PI / 2D, Math.PI, 0);
            Quaternion correctedLocatorQuat = new Quaternion();
            Quaternion.mul(this.parentLocator.getModelSpaceYXZQuaternion(), correction, correctedLocatorQuat);
            Quaternion.normalise(correctedLocatorQuat, correctedLocatorQuat);

            //proceed
            Vec3d forwardVec = new Vec3d(0, 0, -1); //good ol point northward vector
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, correctedLocatorQuat).normalize();
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, QuaternionUtils.createYXZQuaternion(0, finalYawRadians, 0)).normalize();
            this.directionVector = forwardVec;
        }

        //-----update segment list-----
        Iterator<RiftLibRaySegment> it = this.raySegmentList.iterator();
        while (it.hasNext()) {
            RiftLibRaySegment raySegment = it.next();

            //update
            raySegment.onUpdate();
            if (this.rayShape.followUserRotation()) raySegment.updateDirectionVector(this.directionVector);
        }

        //-----create a new segment, rate is dependent on ray speed-----
        if (!this.isEnded && this.originPos != null) {
            this.raySegmentList.add(new RiftLibRaySegment(
                    this.rayCreator, this.originPos, this.directionVector, this.rayShape,
                    this.maxRayLength, this.rayWidthRange, this.raySpeed, this.spreadOnHitBlock,
                    this.breakBlockCondition
            ));

            if (this.onlyOneSegment) this.endRay();
        }

        //-----send ray information to the ray creator-----
        if (!this.rayCreator.getRayCreator().world.isRemote && this.originPos != null) {
            this.rayCreator.applyRaySegments(
                    this.rayName, new BlockPos(this.originPos),
                    new RayHitResult(this.getEntitiesInRaySegments(), this.getBlockPositionsInRaySegments())
            );
        }

        //-----remove from list of segments if dead after callers had a tick to consume their final state-----
        it = this.raySegmentList.iterator();
        while (it.hasNext()) {
            RiftLibRaySegment raySegment = it.next();
            if (raySegment.isDead()) it.remove();
        }

        if (this.isEnded && this.raySegmentList.isEmpty()) this.killRay();
    }

    /**
     * Use this to end this ray to make it fade out.
     * */
    public void endRay() {
        this.isEnded = true;
    }

    /**
     * Use this to kill this ray and make it just disappear.
     * */
    public void killRay() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public List<AxisAlignedBB> getSegmentAABBList() {
        List<AxisAlignedBB> toReturn = new ArrayList<>();
        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            toReturn.addAll(raySegment.getSegmentAABBList());
        }
        return toReturn;
    }

    private Set<Entity> getEntitiesInRaySegments() {
        List<AxisAlignedBB> aabbList = this.getSegmentAABBList();
        Set<Entity> toReturn = new HashSet<>();
        World world = this.rayCreator.getRayCreator().world;

        for (AxisAlignedBB aabb : aabbList) {
            List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, aabb);
            for (Entity entity : entityList) {
                if (entity.equals(this.rayCreator.getRayCreator())) continue;
                if (toReturn.contains(entity)) continue;
                toReturn.add(entity);
            }
        }

        return toReturn;
    }

    private Set<BlockPos> getBlockPositionsInRaySegments() {
        List<AxisAlignedBB> aabbList = this.getSegmentAABBList();
        Set<BlockPos> toReturn = new HashSet<>();

        for (AxisAlignedBB aabb : aabbList) {
            int minX = MathHelper.floor(aabb.minX);
            int minY = MathHelper.floor(aabb.minY);
            int minZ = MathHelper.floor(aabb.minZ);
            int maxX = MathHelper.floor(aabb.maxX);
            int maxY = MathHelper.floor(aabb.maxY);
            int maxZ = MathHelper.floor(aabb.maxZ);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos testPos = new BlockPos(x, y, z);
                        if (!aabb.intersects(x, y, z, x + 1D, y + 1D, z + 1D)) continue;
                        toReturn.add(testPos);
                    }
                }
            }
        }

        return toReturn;
    }

    /**
     * This is for the shape of the ray.
     * */
    public enum RayType {
        /**
         * A spray is made of AABBs traveling along a straight line, and will not
         * instantly rotate alongside the user's rotations.
         * */
        SPRAY(new RiftLibRayMovingShape()),
        /**
         * A fixed beam is made of AABBs forming a fixed straight line, and will
         * instantly rotate alongside the user's rotations.
         * */
        BEAM(new RiftLibRayBeamShape()),
        /**
         * Impact only, does not move.
         */
        IMPACT_SPHERE(new RiftLibRaySphereImpactShape()),
        /**
         * Impact only, does not move, and only spreads across the upper sphere.
         */
        IMPACT_UPPER_SPHERE(new RiftLibRayUpperSphereImpactShape());

        @NotNull
        private final RiftLibRayShape shape;

        RayType(@NotNull RiftLibRayShape shape) {
            this.shape = shape;
        }

        @NotNull
        public RiftLibRayShape getShape() {
            return this.shape;
        }
    }

    /**
     * A ray builder is like a template but for the rays to create.
     * */
    public static class Builder {
        @NotNull
        public final IRayCreator<?> rayCreator;

        @Nullable
        private RayType rayType;
        private double maxLength = 1;
        private double minWidth = 1;
        private double maxWidth = 1;

        private double raySpeed = 1D;

        private boolean spreadOnHitBlock;

        private boolean onlyOneSegment;

        @NotNull
        private Function<BlockPos, Boolean> breakBlockCondition = pos -> false;

        public Builder(@NotNull IRayCreator<?> rayCreator) {
            this.rayCreator = rayCreator;
        }

        public Builder setShapeSpray(double maxLength, double width) {
            if (this.rayType != null) {
                RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
                return this;
            }
            this.rayType = RayType.SPRAY;
            this.maxLength = maxLength;
            this.minWidth = width;
            this.maxWidth = width;
            return this;
        }

        public Builder setShapeSpray(double maxLength, double minWidth, double maxWidth) {
            if (this.rayType != null) {
                RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
                return this;
            }
            if (maxWidth < minWidth) {
                RiftLib.LOGGER.error("Ray max width {} is smaller than min width {}. Skipping...", maxWidth, minWidth);
                return this;
            }
            this.rayType = RayType.SPRAY;
            this.maxLength = maxLength;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder setShapeBeam(double maxLength, double width) {
            if (this.rayType != null) {
                RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
                return this;
            }
            if (maxWidth < minWidth) {
                RiftLib.LOGGER.error("Ray max width {} is smaller than min width {}. Skipping...", maxWidth, minWidth);
                return this;
            }
            this.rayType = RayType.BEAM;
            this.maxLength = maxLength;
            this.minWidth = width;
            this.maxWidth = width;
            return this;
        }

        public Builder setShapeBeam(double maxLength, double minWidth, double maxWidth) {
            if (this.rayType != null) {
                RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
                return this;
            }
            if (maxWidth < minWidth) {
                RiftLib.LOGGER.error("Ray max width {} is smaller than min width {}. Skipping...", maxWidth, minWidth);
                return this;
            }
            this.rayType = RayType.BEAM;
            this.maxLength = maxLength;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder setShapeImpact(double startWidth, double endWidth) {
            return this.setImpactShape(RayType.IMPACT_SPHERE, startWidth, endWidth);
        }

        public Builder setShapeUpperImpact(double startWidth, double endWidth) {
            return this.setImpactShape(RayType.IMPACT_UPPER_SPHERE, startWidth, endWidth);
        }

        private Builder setImpactShape(@NotNull RayType rayType, double startWidth, double endWidth) {
            if (this.rayType != null) {
                RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
                return this;
            }
            if (endWidth < startWidth) {
                RiftLib.LOGGER.error("Impact ray end width {} is smaller than start width {}. Skipping...", endWidth, startWidth);
                return this;
            }
            this.rayType = rayType;
            this.minWidth = startWidth;
            this.maxWidth = Math.max(startWidth, endWidth);
            return this;
        }

        @Nullable
        public RayType getRayType() {
            return this.rayType;
        }

        public double[] getRayWidthRange() {
            return new double[]{this.minWidth, this.maxWidth};
        }

        public double getRayMaxLength() {
            return this.maxLength;
        }

        public Builder setRaySpeed(double value) {
            this.raySpeed = value;
            return this;
        }

        public double getRaySpeed() {
            return this.raySpeed;
        }

        public Builder setSpreadOnHitBlock() {
            if (this.rayType != null && this.rayType.shape.startsAsImpact()) {
                RiftLib.LOGGER.warn("Impact type rays do not travel, so using setSpreadOnHitBlock() is unnecessary.");
                return this;
            }
            this.spreadOnHitBlock = true;
            return this;
        }

        public boolean getSpreadOnHitBlock() {
            return this.spreadOnHitBlock;
        }

        public Builder setBreakBlockCondition(@NotNull Function<BlockPos, Boolean> value) {
            this.breakBlockCondition = value;
            return this;
        }

        @NotNull
        public Function<BlockPos, Boolean> getBreakBlockCondition() {
            return this.breakBlockCondition;
        }

        public Builder setOnlyOneSegment() {
            this.onlyOneSegment = true;
            return this;
        }

        public boolean getOnlyOneSegment() {
            return this.onlyOneSegment;
        }
    }

    /**
     * This ray result is to be immutable and to be sent to the server.
     * */
    public record RayHitResult(Set<Entity> hitEntities, Set<BlockPos> hitBlockPositions) {}
}
