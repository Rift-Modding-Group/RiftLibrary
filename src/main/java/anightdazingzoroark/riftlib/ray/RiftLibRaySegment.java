package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

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
    private final IRayCreator<?> rayCreator;
    @NotNull
    private final Vec3d initPos;
    @NotNull
    private Vec3d directionVector; //is expected to be a unit vector
    @NotNull
    private final RiftLibRay.RayType rayType;

    private final double maxRayLength;
    private final double @NotNull [] rayWidthRange;
    private final double raySpeed;
    private final boolean spreadOnHitBlock;
    @NotNull
    private final Function<BlockPos, Boolean> breakBlockCondition;

    private Vec3d currentPos;
    private double currentWidth;
    private double distanceTravelled;
    private double impactMaxRadius;
    private double impactDepth;
    private int impactLayer;
    private double impactCurrentRadius;
    private double impactDecayRadius;
    private BlockPos impactOriginPos;
    @Nullable
    private EnumFacing impactFace;

    private boolean isImpacting;
    private boolean isDead;

    public RiftLibRaySegment(
            @NotNull IRayCreator<?> rayCreator, @NotNull Vec3d initPos, @NotNull Vec3d directionVector, @NotNull RiftLibRay.RayType rayType,
            double maxRayLength, double @NotNull [] rayWidthRange, double raySpeed, boolean spreadOnHitBlock,
            @NotNull Function<BlockPos, Boolean> breakBlockCondition
    ) {
        this.rayCreator = rayCreator;
        this.initPos = initPos;
        this.directionVector = directionVector.normalize();
        this.rayType = rayType;
        this.maxRayLength = maxRayLength;
        this.rayWidthRange = rayWidthRange;
        this.raySpeed = raySpeed;
        this.spreadOnHitBlock = spreadOnHitBlock;
        this.breakBlockCondition = breakBlockCondition;
        this.currentPos = initPos;
        this.currentWidth = rayWidthRange[0];

        //non impact types start out moving, and thus, require an AABB
        if (this.rayType != RiftLibRay.RayType.IMPACT) {
            this.segmentAABB = new AxisAlignedBB(
                    initPos.x - rayWidthRange[0] / 2D,
                    initPos.y - rayWidthRange[0] / 2D,
                    initPos.z - rayWidthRange[0] / 2D,
                    initPos.x + rayWidthRange[0] / 2D,
                    initPos.y + rayWidthRange[0] / 2D,
                    initPos.z + rayWidthRange[0] / 2D
            );
        }
        //impact types require starting to impact
        else this.startImpactImpactType(new BlockPos(initPos));
    }

    /**
     * This updates the segment and is meant to be called from the parent ray.
     * */
    public void onUpdate() {
        //-----do not update if segment is dead-----
        if (this.isDead) return;

        //-----logic when impacting-----
        if (this.isImpacting) {
            //impact type ray specific logic
            if (this.rayType == RiftLibRay.RayType.IMPACT) {
                //normal operation within limits
                if (this.impactCurrentRadius < this.impactMaxRadius) {
                    //define previous and current target layers
                    double previousRadius = this.impactCurrentRadius;
                    double targetRadius = Math.min(this.impactMaxRadius, previousRadius + Math.max(0D, this.raySpeed));

                    //update spherical impact layers
                    int radiusBlocks = (int) Math.ceil(targetRadius);
                    double previousRadiusSq = previousRadius * previousRadius;
                    double targetRadiusSq = targetRadius * targetRadius;

                    for (int x = -radiusBlocks; x <= radiusBlocks; x++) {
                        for (int y = -radiusBlocks; y <= radiusBlocks; y++) {
                            for (int z = -radiusBlocks; z <= radiusBlocks; z++) {
                                int distanceSq = x * x + y * y + z * z;
                                if (distanceSq <= previousRadiusSq || distanceSq > targetRadiusSq) continue;

                                BlockPos impactPos = this.impactOriginPos.add(x, y, z);
                                if (!this.canImpactReachPosition(impactPos)) continue;
                                this.tryAddImpactPosition(impactPos);
                            }
                        }
                    }
                    this.impactCurrentRadius = targetRadius;
                }

                //decay impact positions
                this.decayImpactPositions();

                //kill upon going beyond bounds
                if (this.impactCurrentRadius >= this.impactMaxRadius && this.segmentImpactPositions.isEmpty()) this.killSegment();
            }
            //for other ray types
            else {
                int maxLayer = (int) Math.ceil(this.impactMaxRadius + this.impactDepth);

                if (this.impactLayer < maxLayer && !this.impactFrontier.isEmpty()) {
                    //define spread progress and target layer
                    this.impactCurrentRadius += Math.max(0D, this.raySpeed);
                    int targetLayer = Math.min(maxLayer, (int) Math.floor(this.impactCurrentRadius));

                    //update cylindrical impact layers
                    while (this.impactLayer < targetLayer && !this.impactFrontier.isEmpty()) {
                        List<BlockPos> oldFrontier = new ArrayList<>(this.impactFrontier);
                        this.impactFrontier.clear();
                        this.impactLayer++;

                        //cardinal neighbor spread for use in impact from other ray type
                        for (BlockPos frontierPos : oldFrontier) {
                            for (EnumFacing facing : EnumFacing.values()) {
                                BlockPos nextPos = frontierPos.offset(facing);
                                if (!this.canImpactReachPosition(nextPos)) continue;

                                //add upon passing checks
                                this.tryAddImpactPosition(nextPos);
                            }
                        }
                    }
                }

                //decay innermost positions
                this.decayImpactPositions();

                if ((this.impactLayer >= maxLayer || this.impactFrontier.isEmpty()) && this.segmentImpactPositions.isEmpty()) this.killSegment();
            }
        }
        //-----logic when moving-----
        else {
            //kill when going beyond max ray length
            if (this.distanceTravelled >= this.maxRayLength) {
                this.killSegment();
                return;
            }

            //define how many steps to take and length of each step
            double stepLength = Math.min(this.raySpeed, this.maxRayLength - this.distanceTravelled);
            int steps = Math.max(1, (int)Math.ceil(stepLength / 0.25D));

            //walk along each step
            for (int i = 1; i <= steps; i++) {
                double stepDistance = this.distanceTravelled + stepLength * i / (double)steps;
                this.moveToDistance(stepDistance);

                ImmutablePair<BlockPos, EnumFacing> impactHit = this.findImpactHit();
                if (impactHit != null) {
                    this.distanceTravelled = stepDistance;
                    if (this.spreadOnHitBlock) this.startImpactNonImpactType(impactHit.getLeft(), impactHit.getRight());
                    else this.killSegment();
                    return;
                }
            }

            //update distance traveled
            this.distanceTravelled += stepLength;
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

    public List<AxisAlignedBB> getSegmentAABBList() {
        List<AxisAlignedBB> toReturn = new ArrayList<>();
        //ignore segment aabb if impacting
        if (!this.isImpacting) toReturn.add(this.segmentAABB);

        for (BlockPos impactPos : this.segmentImpactPositions) {
            toReturn.add(new AxisAlignedBB(
                    impactPos.getX(), impactPos.getY(), impactPos.getZ(),
                    impactPos.getX() + 1D, impactPos.getY() + 1D, impactPos.getZ() + 1D
            ));
        }

        return toReturn;
    }

    private void moveToDistance(double distance) {
        this.currentPos = this.initPos.add(this.directionVector.scale(distance));
        this.currentWidth = MathUtils.slopeResult(distance, true, 0, this.maxRayLength, this.rayWidthRange[0], this.rayWidthRange[1]);

        this.segmentAABB = new AxisAlignedBB(
                this.currentPos.x - this.currentWidth / 2D,
                this.currentPos.y - this.currentWidth / 2D,
                this.currentPos.z - this.currentWidth / 2D,
                this.currentPos.x + this.currentWidth / 2D,
                this.currentPos.y + this.currentWidth / 2D,
                this.currentPos.z + this.currentWidth / 2D
        );
    }

    /**
     * Create an impact hit, represented by an ImmutablePair with a BlockPos
     * and an EnumFacing. For use by rays in motion.
     * */
    private ImmutablePair<BlockPos, EnumFacing> findImpactHit() {
        int minX = MathHelper.floor(this.segmentAABB.minX);
        int minY = MathHelper.floor(this.segmentAABB.minY);
        int minZ = MathHelper.floor(this.segmentAABB.minZ);
        int maxX = MathHelper.floor(this.segmentAABB.maxX);
        int maxY = MathHelper.floor(this.segmentAABB.maxY);
        int maxZ = MathHelper.floor(this.segmentAABB.maxZ);

        World world = this.rayCreator.getRayCreator().world;
        BlockPos bestPos = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos testPos = new BlockPos(x, y, z);
                    if (!world.isBlockLoaded(testPos)) continue;

                    IBlockState state = world.getBlockState(testPos);
                    if (!this.isSolidBlockingBlock(world, testPos, state)) continue;

                    AxisAlignedBB blockBox = state.getCollisionBoundingBox(world, testPos);
                    if (blockBox == null) continue;

                    if (!this.segmentAABB.intersects(
                            blockBox.minX + x, blockBox.minY + y, blockBox.minZ + z,
                            blockBox.maxX + x, blockBox.maxY + y, blockBox.maxZ + z
                    )) {
                        continue;
                    }

                    double distanceSq = this.currentPos.squareDistanceTo(
                            x + 0.5D,
                            y + 0.5D,
                            z + 0.5D
                    );
                    if (distanceSq < bestDistanceSq) {
                        bestDistanceSq = distanceSq;
                        bestPos = testPos;
                    }
                }
            }
        }

        if (bestPos == null) return null;
        return new ImmutablePair<>(
                bestPos,
                //is basically the complete opposite of the direction vector, defines
                //the side that got impacted
                EnumFacing.getFacingFromVector(
                    (float)-this.directionVector.x,
                    (float)-this.directionVector.y,
                    (float)-this.directionVector.z
                )
        );
    }

    /**
     * Create an impact for non impact type rays
     * */
    private void startImpactNonImpactType(@NotNull BlockPos hitBlockPos, @NotNull EnumFacing face) {
        this.isImpacting = true;
        this.impactFace = face;
        this.impactMaxRadius = Math.max(1D, this.maxRayLength - this.distanceTravelled);
        this.impactDepth = Math.max(1D, this.currentWidth);
        this.impactLayer = 0;
        this.impactCurrentRadius = 0D;
        this.impactDecayRadius = 0D;
        this.segmentImpactPositions.clear();
        this.expiredImpactPositions.clear();
        this.impactFrontier.clear();

        BlockPos startPos = hitBlockPos.offset(face);
        this.impactOriginPos = startPos;

        this.tryAddImpactPosition(startPos);

        double startingRadius = this.currentWidth / 2D;
        int startingRadiusBlocks = (int) Math.ceil(startingRadius);

        for (int x = -startingRadiusBlocks; x <= startingRadiusBlocks; x++) {
            for (int y = -startingRadiusBlocks; y <= startingRadiusBlocks; y++) {
                for (int z = -startingRadiusBlocks; z <= startingRadiusBlocks; z++) {
                    //block if not on cylinder impact plane for non impact type rays
                    if (x * face.getXOffset() + y * face.getYOffset() + z * face.getZOffset() == 0) continue;

                    //add once passed
                    this.tryAddImpactPosition(startPos.add(x, y, z));
                }
            }
        }

        //kill if impact frontier somehow is empty
        if (this.impactFrontier.isEmpty()) this.killSegment();
    }

    /**
     * Create an impact for impact type rays
     * */
    private void startImpactImpactType(@NotNull BlockPos hitBlockPos) {
        this.isImpacting = true;
        this.impactMaxRadius = this.rayWidthRange[1];
        this.impactDepth = Math.max(1D, this.currentWidth);
        this.impactLayer = 0;
        this.impactCurrentRadius =  this.currentWidth / 2D;
        this.impactDecayRadius = -Math.max(1D, Math.max(0D, this.raySpeed));
        this.segmentImpactPositions.clear();
        this.expiredImpactPositions.clear();
        this.impactFrontier.clear();

        this.impactOriginPos = hitBlockPos;

        this.tryAddImpactPosition(hitBlockPos);

        double startingRadius = this.currentWidth / 2D;
        int startingRadiusBlocks = (int) Math.ceil(startingRadius);

        for (int x = -startingRadiusBlocks; x <= startingRadiusBlocks; x++) {
            for (int y = -startingRadiusBlocks; y <= startingRadiusBlocks; y++) {
                for (int z = -startingRadiusBlocks; z <= startingRadiusBlocks; z++) {
                    //block if out of reach of sphere plane for impact type rays
                    if (x * x + y * y + z * z > startingRadius * startingRadius) continue;

                    //add once passed
                    this.tryAddImpactPosition(hitBlockPos.add(x, y, z));
                }
            }
        }
    }

    /**
     * To use while updating impact layers. For testing if it the provided target position
     * can be reached.
     * */
    private boolean canImpactReachPosition(@NotNull BlockPos targetPos) {
        if (this.impactOriginPos == null) return false;

        //common stuff
        int relX = targetPos.getX() - this.impactOriginPos.getX();
        int relY = targetPos.getY() - this.impactOriginPos.getY();
        int relZ = targetPos.getZ() - this.impactOriginPos.getZ();
        int relMagSq = relX * relX + relY * relY + relZ * relZ;

        //---step 1: search within shapes---
        //spherical search for impact type rays
        if (this.rayType == RiftLibRay.RayType.IMPACT) {
            if (relMagSq > this.impactMaxRadius * this.impactMaxRadius) return false;
        }
        //cylindrical search for other ray types
        else {
            int faceXOffset = this.impactFace != null ? this.impactFace.getXOffset() : 0;
            int faceYOffset = this.impactFace != null ? this.impactFace.getYOffset() : 0;
            int faceZOffset = this.impactFace != null ? this.impactFace.getZOffset() : 0;

            int inwardDot = -(relX * faceXOffset + relY * faceYOffset + relZ * faceZOffset);
            if (inwardDot < 0 || inwardDot > this.impactDepth) return false;

            int planeX = relX - inwardDot * -faceXOffset;
            int planeY = relY - inwardDot * -faceYOffset;
            int planeZ = relZ - inwardDot * -faceZOffset;

            if (planeX * planeX + planeY * planeY + planeZ * planeZ > this.impactMaxRadius * this.impactMaxRadius) return false;
        }

        //---step 2: test if it is possible to go to that position from origin---
        if (targetPos.equals(this.impactOriginPos)) return true;

        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(relMagSq) * 4D));
        World world = this.rayCreator.getRayCreator().world;

        for (int i = 1; i < steps; i++) {
            double progress = i / (double) steps;
            BlockPos testPos = new BlockPos(
                    MathHelper.floor(this.impactOriginPos.getX() + 0.5D + relX * progress),
                    MathHelper.floor(this.impactOriginPos.getY() + 0.5D + relY * progress),
                    MathHelper.floor(this.impactOriginPos.getZ() + 0.5D + relZ * progress)
            );

            if (testPos.equals(this.impactOriginPos) || testPos.equals(targetPos)) continue;
            if (this.canImpactContinueThrough(testPos, world)) continue;

            return false;
        }

        return true;
    }

    /**
     * Safely test if it is possible to add an impact position and break it.
     * */
    private void tryAddImpactPosition(@NotNull BlockPos pos) {
        //-----test for if it has already been added-----
        if (this.expiredImpactPositions.contains(pos) || !this.segmentImpactPositions.add(pos)) return;
        this.segmentImpactPositions.add(pos);

        //-----test for if the impact can continue through block-----
        World world = this.rayCreator.getRayCreator().world;
        if (!this.canImpactContinueThrough(pos, world)) return;

        //-----break block on server-----
        IBlockState state = world.getBlockState(pos);
        if (!world.isRemote && state.getMaterial() != Material.AIR && state.getMaterial() != Material.FIRE) {
            world.destroyBlock(pos, true);
        }

        //-----add to impact frontier-----
        if (this.rayType != RiftLibRay.RayType.IMPACT) this.impactFrontier.add(pos);
    }

    /**
     * Provide decay for impact results
     * */
    private void decayImpactPositions() {
        if (this.segmentImpactPositions.isEmpty()) return;

        this.impactDecayRadius += Math.max(0D, this.raySpeed);
        if (this.impactDecayRadius <= 0D) return;

        Iterator<BlockPos> it = this.segmentImpactPositions.iterator();

        while (it.hasNext()) {
            BlockPos impactPos = it.next();
            if (!this.isWithinImpactDecayFront(impactPos)) continue;

            it.remove();
            this.expiredImpactPositions.add(impactPos);
        }
    }

    /**
     * Test if block pos is within impact decay front
     * */
    private boolean isWithinImpactDecayFront(@NotNull BlockPos pos) {
        if (this.impactOriginPos == null) return false;

        int relX = pos.getX() - this.impactOriginPos.getX();
        int relY = pos.getY() - this.impactOriginPos.getY();
        int relZ = pos.getZ() - this.impactOriginPos.getZ();

        if (this.rayType == RiftLibRay.RayType.IMPACT) return relX * relX + relY * relY + relZ * relZ <= this.impactDecayRadius * this.impactDecayRadius;
        else return Math.abs(relX) + Math.abs(relY) + Math.abs(relZ) <= this.impactDecayRadius;
    }

    private boolean canImpactContinueThrough(@NotNull BlockPos pos, @NotNull World world) {
        IBlockState state = world.getBlockState(pos);
        if (!this.isAlwaysBreakableForImpact(world, pos, state) && !this.breakBlockCondition.apply(pos)) return false;
        return !this.hasDiagonalUnbreakableConnectionAround(pos, world);
    }

    /**
     * Make sure that breakable blocks that have at least 2 adjacent unbreakable blocks
     * forming a diagonal do not get broken.
     * */
    private boolean hasDiagonalUnbreakableConnectionAround(@NotNull BlockPos pos, @NotNull World world) {
        EnumFacing[] facings = EnumFacing.values();

        for (int i = 0; i < facings.length; i++) {
            EnumFacing firstFacing = facings[i];
            BlockPos firstPos = pos.offset(firstFacing);
            if (!this.isUnbreakableForImpact(firstPos, world)) continue;

            for (int j = i + 1; j < facings.length; j++) {
                EnumFacing secondFacing = facings[j];
                if (firstFacing.getAxis() == secondFacing.getAxis()) continue;

                BlockPos secondPos = pos.offset(secondFacing);
                if (this.isUnbreakableForImpact(secondPos, world)) return true;
            }
        }

        return false;
    }

    private boolean isUnbreakableForImpact(@NotNull BlockPos pos, @NotNull World world) {
        IBlockState state = world.getBlockState(pos);
        return !this.isAlwaysBreakableForImpact(world, pos, state) && !this.breakBlockCondition.apply(pos);
    }

    private boolean isAlwaysBreakableForImpact(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.FIRE || !this.isSolidBlockingBlock(world, pos, state);
    }

    private boolean isSolidBlockingBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial().isSolid() && state.getCollisionBoundingBox(world, pos) != null;
    }
}
