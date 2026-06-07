package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibThreeDimShape;
import anightdazingzoroark.riftlib.util.MathUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
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
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * This abstract class is the main template for ray shapes. Ray shapes determine
 * ray segment behavior and the shape of the impact.
 * */
public abstract class RiftLibRayShape {
    //common shape details for impacts
    @Nullable
    protected Vec3d impactOriginVec;
    @Nullable
    protected RiftLibThreeDimShape impactShape;
    @NotNull
    protected Quaternion impactShapeRotation = new Quaternion();

    /**
     * Runs the moment a segment is created.
     * */
    public abstract void onCreateSegment(@NotNull RiftLibRaySegment segment);

    /**
     * For updating a ray segment when it is traveling.
     * */
    public void updateMovingShape(@NotNull RiftLibRaySegment segment) {}

    /**
     * Create an impact hit while the ray is traveling. Shapes that do not move
     * should keep the default null result.
     * */
    @Nullable
    public ImmutablePair<BlockPos, EnumFacing> findImpactHit(@NotNull RiftLibRaySegment segment) {
        return null;
    }

    /**
     * For handling a block hit while the ray is traveling.
     * */
    public void startImpact(@NotNull RiftLibRaySegment segment, @NotNull BlockPos hitBlockPos, @NotNull EnumFacing face) {
        segment.killSegment();
    }

    /**
     * For updating a ray segment when it is impacting.
     * */
    public abstract void updateImpact(@NotNull RiftLibRaySegment segment);

    /**
     * Test if a position is within the current decay front and should expire.
     * */
    public abstract boolean isWithinImpactDecayFront(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos);

    /**
     * Current shape used by common mesh queries.
     * */
    @Nullable
    protected RiftLibThreeDimShape getCurrentShape(@NotNull RiftLibRaySegment segment) {
        return this.impactShape;
    }

    /**
     * For adding impact positions using the current impact shape.
     * */
    protected void addImpactPositionsWithinCurrentShape(@NotNull RiftLibRaySegment segment) {
        if (this.impactShape == null) return;

        for (BlockPos impactPos : this.getBlockPositionsInShape(segment, this.impactShape)) {
            if (!this.canImpactReachPosition(segment, impactPos)) continue;
            this.tryAddImpactPosition(segment, impactPos);
        }
    }

    /**
     * Safely test if it is possible to add an impact position and break it.
     * */
    protected void tryAddImpactPosition(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        //-----test for if it has already been added-----
        if (segment.getExpiredImpactPositions().contains(pos) || segment.getSegmentImpactPositions().contains(pos)) return;

        //-----test for if the impact can continue through block-----
        if (!this.canImpactContinueThrough(segment, pos)) return;

        segment.getSegmentImpactPositions().add(pos);

        //-----break block on server-----
        World world = segment.rayCreator.getRayCreator().world;
        IBlockState state = world.getBlockState(pos);
        if (!world.isRemote && state.getMaterial() != Material.AIR && state.getMaterial() != Material.FIRE) {
            world.destroyBlock(pos, true);
        }
    }

    /**
     * Return block positions in this ray shape's current mesh.
     * */
    @NotNull
    public Set<BlockPos> getBlockPositionsInCurrentShape(@NotNull RiftLibRaySegment segment) {
        if (segment.isImpacting()) return new HashSet<>(segment.getSegmentImpactPositions());

        RiftLibThreeDimShape shape = this.getCurrentShape(segment);
        if (shape == null) return Collections.emptySet();
        return this.getBlockPositionsInShape(segment, shape);
    }

    /**
     * Test an entity against this ray shape's current mesh.
     * */
    public boolean intersectsEntity(@NotNull RiftLibRaySegment segment, @NotNull Entity entity) {
        RiftLibThreeDimShape shape = this.getCurrentShape(segment);
        if (shape == null) return false;

        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        if (!segment.getSegmentAABB().intersects(entityBox)) return false;

        for (Vec3d samplePoint : this.getAABBSamplePoints(entityBox)) {
            if (segment.isImpacting() && !this.isImpactWorldPointReachable(segment, samplePoint)) continue;
            if (shape.contains(samplePoint)) return true;
        }

        return false;
    }

    /**
     * Return debug grid lines for the segment's current mesh.
     * */
    @NotNull
    public List<RiftLibRaySegment.DebugLine> getDebugGridLines(@NotNull RiftLibRaySegment segment) {
        List<RiftLibRaySegment.DebugLine> debugLines = this.createDebugGridLines(segment);
        if (!segment.isImpacting()) return debugLines;

        List<RiftLibRaySegment.DebugLine> reachableLines = new ArrayList<>();
        for (RiftLibRaySegment.DebugLine debugLine : debugLines) {
            if (!this.isImpactDebugLineReachable(segment, debugLine)) continue;
            reachableLines.add(debugLine);
        }

        return reachableLines;
    }

    /**
     * Create unfiltered debug grid lines. Subclasses own the mesh-specific line generation.
     * */
    @NotNull
    protected List<RiftLibRaySegment.DebugLine> createDebugGridLines(@NotNull RiftLibRaySegment segment) {
        return Collections.emptyList();
    }

    //-----debug grid line helpers-----
    protected void addTransformedLineLoop(
            @NotNull List<RiftLibRaySegment.DebugLine> lines, @NotNull Vec3d origin, @NotNull Quaternion rotation,
            @NotNull Vec3d first, @NotNull Vec3d second, @NotNull Vec3d third, @NotNull Vec3d fourth
    ) {
        this.addTransformedLine(lines, origin, rotation, first, second);
        this.addTransformedLine(lines, origin, rotation, second, third);
        this.addTransformedLine(lines, origin, rotation, third, fourth);
        this.addTransformedLine(lines, origin, rotation, fourth, first);
    }

    protected void addTransformedLine(
            @NotNull List<RiftLibRaySegment.DebugLine> lines, @NotNull Vec3d origin, @NotNull Quaternion rotation,
            @NotNull Vec3d start, @NotNull Vec3d end
    ) {
        lines.add(new RiftLibRaySegment.DebugLine(
                this.transformShapeOffset(origin, start, rotation),
                this.transformShapeOffset(origin, end, rotation)
        ));
    }

    //-----helpers from here on out-----
    @NotNull
    public Quaternion getImpactShapeRotation() {
        return this.impactShapeRotation;
    }

    @NotNull
    protected Vec3d transformShapeOffset(@NotNull Vec3d origin, @NotNull Vec3d offset, @NotNull Quaternion rotation) {
        Vec3d rotatedOffset = VectorUtils.rotateVectorWithQuaternion(offset, rotation);
        return origin.add(rotatedOffset);
    }

    /**
     * To use while updating impact layers. For testing if the provided target position
     * can be reached.
     * */
    protected boolean canImpactReachPosition(@NotNull RiftLibRaySegment segment, @NotNull BlockPos targetPos) {
        if (this.impactOriginVec == null) return false;

        //---step 1: search within shapes---
        if (this.impactShape == null || !this.blockIntersectsShape(segment, targetPos, this.impactShape)) return false;

        //---step 2: test if it is possible to go to that position from origin---
        BlockPos impactOriginPos = new BlockPos(this.impactOriginVec);
        if (targetPos.equals(impactOriginPos)) return true;

        //-----step 3: perform the test as expected-----
        int relX = targetPos.getX() - impactOriginPos.getX();
        int relY = targetPos.getY() - impactOriginPos.getY();
        int relZ = targetPos.getZ() - impactOriginPos.getZ();
        int relMagSq = relX * relX + relY * relY + relZ * relZ;
        int steps = Math.max(1, (int)Math.ceil(Math.sqrt(relMagSq) * 4D));

        for (int i = 1; i < steps; i++) {
            BlockPos testPos = new BlockPos(
                    MathHelper.floor(MathUtils.slopeResult(i, true, 0D, steps, impactOriginPos.getX() + 0.5D, targetPos.getX() + 0.5D)),
                    MathHelper.floor(MathUtils.slopeResult(i, true, 0D, steps, impactOriginPos.getY() + 0.5D, targetPos.getY() + 0.5D)),
                    MathHelper.floor(MathUtils.slopeResult(i, true, 0D, steps, impactOriginPos.getZ() + 0.5D, targetPos.getZ() + 0.5D))
            );

            if (testPos.equals(impactOriginPos) || testPos.equals(targetPos)) continue;
            if (this.canImpactContinueThrough(segment, testPos)) continue;

            return false;
        }

        return true;
    }

    @NotNull
    protected Set<BlockPos> getBlockPositionsInShape(@NotNull RiftLibRaySegment segment, @NotNull RiftLibThreeDimShape shape) {
        Set<BlockPos> toReturn = new HashSet<>();

        int minX = MathHelper.floor(segment.getSegmentAABB().minX);
        int minY = MathHelper.floor(segment.getSegmentAABB().minY);
        int minZ = MathHelper.floor(segment.getSegmentAABB().minZ);
        int maxX = MathHelper.floor(segment.getSegmentAABB().maxX);
        int maxY = MathHelper.floor(segment.getSegmentAABB().maxY);
        int maxZ = MathHelper.floor(segment.getSegmentAABB().maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos testPos = new BlockPos(x, y, z);
                    if (!this.blockIntersectsShape(segment, testPos, shape)) continue;
                    toReturn.add(testPos);
                }
            }
        }

        return toReturn;
    }

    protected boolean blockIntersectsShape(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos, @Nullable RiftLibThreeDimShape shape) {
        if (shape == null) return false;

        AxisAlignedBB blockBox = new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1D, pos.getY() + 1D, pos.getZ() + 1D
        );
        if (!segment.getSegmentAABB().intersects(blockBox)) return false;

        for (Vec3d samplePoint : this.getAABBSamplePoints(blockBox)) {
            if (shape.contains(samplePoint)) return true;
        }

        return false;
    }

    @NotNull
    protected List<Vec3d> getAABBSamplePoints(@NotNull AxisAlignedBB box) {
        List<Vec3d> samplePoints = new ArrayList<>();
        double centerX = (box.minX + box.maxX) / 2D;
        double centerY = (box.minY + box.maxY) / 2D;
        double centerZ = (box.minZ + box.maxZ) / 2D;

        samplePoints.add(new Vec3d(centerX, centerY, centerZ));
        samplePoints.add(new Vec3d(box.minX, box.minY, box.minZ));
        samplePoints.add(new Vec3d(box.minX, box.minY, box.maxZ));
        samplePoints.add(new Vec3d(box.minX, box.maxY, box.minZ));
        samplePoints.add(new Vec3d(box.minX, box.maxY, box.maxZ));
        samplePoints.add(new Vec3d(box.maxX, box.minY, box.minZ));
        samplePoints.add(new Vec3d(box.maxX, box.minY, box.maxZ));
        samplePoints.add(new Vec3d(box.maxX, box.maxY, box.minZ));
        samplePoints.add(new Vec3d(box.maxX, box.maxY, box.maxZ));

        return samplePoints;
    }

    protected boolean canImpactContinueThrough(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        World world = segment.rayCreator.getRayCreator().world;
        IBlockState state = world.getBlockState(pos);
        if (!this.isAlwaysBreakableForImpact(world, pos, state) && !segment.builder.getBreakBlockCondition().apply(pos)) return false;
        return !this.hasDiagonalUnbreakableConnectionAround(segment, pos);
    }

    private boolean isUnbreakableForImpact(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        World world = segment.rayCreator.getRayCreator().world;
        IBlockState state = world.getBlockState(pos);
        return !this.isAlwaysBreakableForImpact(world, pos, state) && !segment.builder.getBreakBlockCondition().apply(pos);
    }

    private boolean isAlwaysBreakableForImpact(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.FIRE || !this.isSolidBlockingBlock(world, pos, state);
    }

    protected boolean isSolidBlockingBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        return state.getMaterial().isSolid() && state.getCollisionBoundingBox(world, pos) != null;
    }

    /**
     * Make sure that breakable blocks that have at least 2 adjacent unbreakable blocks
     * forming a diagonal do not get broken.
     * */
    private boolean hasDiagonalUnbreakableConnectionAround(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        EnumFacing[] facings = EnumFacing.values();

        for (int i = 0; i < facings.length; i++) {
            EnumFacing firstFacing = facings[i];
            BlockPos firstPos = pos.offset(firstFacing);
            if (!this.isUnbreakableForImpact(segment, firstPos)) continue;

            for (int j = i + 1; j < facings.length; j++) {
                EnumFacing secondFacing = facings[j];
                if (firstFacing.getAxis() == secondFacing.getAxis()) continue;

                BlockPos secondPos = pos.offset(secondFacing);
                if (this.isUnbreakableForImpact(segment, secondPos)) return true;
            }
        }

        return false;
    }

    private boolean isImpactDebugLineReachable(@NotNull RiftLibRaySegment segment, @NotNull RiftLibRaySegment.DebugLine debugLine) {
        Vec3d start = debugLine.start();
        Vec3d end = debugLine.end();
        Vec3d midpoint = new Vec3d(
                (start.x + end.x) / 2D,
                (start.y + end.y) / 2D,
                (start.z + end.z) / 2D
        );

        return this.isImpactWorldPointReachable(segment, start)
                || this.isImpactWorldPointReachable(segment, midpoint)
                || this.isImpactWorldPointReachable(segment, end);
    }

    private boolean isImpactWorldPointReachable(@NotNull RiftLibRaySegment segment, @NotNull Vec3d point) {
        double epsilon = 1.0E-5D;
        double[] xSamples = new double[]{point.x, point.x - epsilon, point.x + epsilon};
        double[] ySamples = new double[]{point.y, point.y - epsilon, point.y + epsilon};
        double[] zSamples = new double[]{point.z, point.z - epsilon, point.z + epsilon};

        for (double x : xSamples) {
            for (double y : ySamples) {
                for (double z : zSamples) {
                    if (segment.getSegmentImpactPositions().contains(new BlockPos(x, y, z))) return true;
                }
            }
        }

        return false;
    }
}
