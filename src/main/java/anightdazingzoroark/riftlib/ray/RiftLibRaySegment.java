package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.util.MathUtils;
import anightdazingzoroark.riftlib.util.MutableAxisAlignedBB;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Meant to be created on the client. A ray segment represents a collidable
 * area in motion.
 * */
public class RiftLibRaySegment {
    //AABB of the segment when it is traveling
    @NotNull
    private AxisAlignedBB segmentAABB;
    //block positions that represent where a segment hit, they spread out over time and dissipate
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

    private final double maxRayLength;
    private final double @NotNull [] rayWidthRange;
    private final double raySpeed;
    private final boolean spreadOnHitBlock;
    @NotNull
    private final Function<BlockPos, Boolean> breakBlockCondition;

    private Vec3d currentPos;
    private double currentWidth;
    private double distanceTravelled;
    private double impactRadius;
    private double impactDepth;
    private int impactLayer;
    private double impactSpreadProgress;
    private BlockPos impactOriginPos;
    private EnumFacing impactFace;

    private boolean isImpacting;
    private boolean isDead;

    public RiftLibRaySegment(
            @NotNull IRayCreator<?> rayCreator, @NotNull Vec3d initPos, @NotNull Vec3d directionVector,
            double maxRayLength, double @NotNull [] rayWidthRange, double raySpeed, boolean spreadOnHitBlock,
            @NotNull Function<BlockPos, Boolean> breakBlockCondition
    ) {
        this.rayCreator = rayCreator;
        this.initPos = initPos;
        this.directionVector = directionVector.normalize();
        this.maxRayLength = maxRayLength;
        this.rayWidthRange = rayWidthRange;
        this.raySpeed = raySpeed;
        this.spreadOnHitBlock = spreadOnHitBlock;
        this.breakBlockCondition = breakBlockCondition;
        this.currentPos = initPos;
        this.currentWidth = rayWidthRange[0];

        this.segmentAABB = new AxisAlignedBB(
                initPos.x - rayWidthRange[0] / 2D,
                initPos.y - rayWidthRange[0] / 2D,
                initPos.z - rayWidthRange[0] / 2D,
                initPos.x + rayWidthRange[0] / 2D,
                initPos.y + rayWidthRange[0] / 2D,
                initPos.z + rayWidthRange[0] / 2D
        );
    }

    /**
     * This updates the segment and is meant to be called from the parent ray.
     * */
    public void onUpdate() {
        //-----do not update if segment is dead-----
        if (this.isDead) return;

        //-----logic when impacting-----
        if (this.isImpacting) {
            int maxLayer = (int)Math.ceil(this.impactRadius + this.impactDepth);

            //kill when going beyond max ray length
            if (this.impactLayer >= maxLayer || this.impactFrontier.isEmpty()) {
                this.killSegment();
                return;
            }

            //define spread progress and impact layer
            this.impactSpreadProgress += Math.max(0D, this.raySpeed);
            int targetLayer = Math.min(maxLayer, (int)Math.floor(this.impactSpreadProgress));

            //update impact layer
            while (this.impactLayer < targetLayer && !this.impactFrontier.isEmpty()) {
                List<BlockPos> oldFrontier = new ArrayList<>(this.impactFrontier);
                this.impactFrontier.clear();
                this.impactLayer++;

                for (BlockPos frontierPos : oldFrontier) {
                    for (EnumFacing facing : EnumFacing.values()) {
                        BlockPos nextPos = frontierPos.offset(facing);
                        if (!this.isWithinImpactCylinder(nextPos)) continue;
                        this.tryAddImpactPosition(nextPos);
                    }
                }
            }

            this.decayInnermostImpactPosition();
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
                    if (this.spreadOnHitBlock) this.startImpact(impactHit.getLeft(), impactHit.getRight());
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
        toReturn.add(this.segmentAABB);

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
     * and an EnumFacing.
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

    private void startImpact(@NotNull BlockPos hitBlockPos, @NotNull EnumFacing face) {
        this.isImpacting = true;
        this.impactFace = face;
        this.impactRadius = Math.max(1D, this.maxRayLength - this.distanceTravelled);
        this.impactDepth = Math.max(1D, this.currentWidth);
        this.impactLayer = 0;
        this.impactSpreadProgress = 0D;
        this.segmentImpactPositions.clear();
        this.expiredImpactPositions.clear();
        this.impactFrontier.clear();

        BlockPos startPos = hitBlockPos.offset(face);
        this.impactOriginPos = startPos;
        int startingRadiusBlocks = (int)Math.ceil(this.currentWidth / 2D);

        this.tryAddImpactPosition(startPos);

        for (int x = -startingRadiusBlocks; x <= startingRadiusBlocks; x++) {
            for (int y = -startingRadiusBlocks; y <= startingRadiusBlocks; y++) {
                for (int z = -startingRadiusBlocks; z <= startingRadiusBlocks; z++) {
                    if (!this.isOnImpactPlane(x, y, z)) continue;
                    if (x * x + y * y + z * z > this.currentWidth * this.currentWidth / 4D) continue;
                    this.tryAddImpactPosition(startPos.add(x, y, z));
                }
            }
        }

        if (this.impactFrontier.isEmpty()) this.killSegment();
    }

    private void tryAddImpactPosition(@NotNull BlockPos pos) {
        //-----test for if it has already been added-----
        if (this.expiredImpactPositions.contains(pos) || !this.segmentImpactPositions.add(pos)) return;
        this.segmentImpactPositions.add(pos);

        //-----test for if the block has been loaded-----
        World world = this.rayCreator.getRayCreator().world;
        if (!world.isBlockLoaded(pos)) return;

        //-----test for if the impact can continue through block-----
        IBlockState state = world.getBlockState(pos);
        if (!this.isAlwaysBreakableForImpact(world, pos, state) && !this.breakBlockCondition.apply(pos)) return;

        //-----break block on server-----
        if (!world.isRemote && state.getMaterial() != Material.AIR) {
            world.destroyBlock(pos, true);
        }

        //-----add to impact frontier-----
        this.impactFrontier.add(pos);
    }

    private void decayInnermostImpactPosition() {
        if (this.segmentImpactPositions.isEmpty()) return;

        BlockPos expiredPos = this.segmentImpactPositions.removeFirst();
        this.segmentImpactPositions.remove(expiredPos);
        this.expiredImpactPositions.add(expiredPos);
    }

    private boolean isWithinImpactCylinder(@NotNull BlockPos pos) {
        if (this.impactOriginPos == null) return false;

        int relX = pos.getX() - this.impactOriginPos.getX();
        int relY = pos.getY() - this.impactOriginPos.getY();
        int relZ = pos.getZ() - this.impactOriginPos.getZ();

        int inwardDot = -(relX * this.impactFace.getXOffset() + relY * this.impactFace.getYOffset() + relZ * this.impactFace.getZOffset());
        if (inwardDot < 0 || inwardDot > this.impactDepth) return false;

        int planeX = relX - inwardDot * -this.impactFace.getXOffset();
        int planeY = relY - inwardDot * -this.impactFace.getYOffset();
        int planeZ = relZ - inwardDot * -this.impactFace.getZOffset();

        return planeX * planeX + planeY * planeY + planeZ * planeZ <= this.impactRadius * this.impactRadius;
    }

    private boolean isOnImpactPlane(int relX, int relY, int relZ) {
        return relX * this.impactFace.getXOffset() + relY * this.impactFace.getYOffset() + relZ * this.impactFace.getZOffset() == 0;
    }

    private boolean isAlwaysBreakableForImpact(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial() == Material.AIR || !this.isSolidBlockingBlock(world, pos, state);
    }

    private boolean isSolidBlockingBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial().isSolid() && state.getCollisionBoundingBox(world, pos) != null;
    }
}
