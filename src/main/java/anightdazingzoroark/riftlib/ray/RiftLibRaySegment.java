package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.ray.rayShape.RiftLibRayGeometry;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibImpactShape;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A ray segment that can either travel through the world or expand as an impact.
 */
public class RiftLibRaySegment {
    @NotNull
    private final IRayCreator<?> rayCreator;
    @NotNull
    private final RiftLibRayBuilder builder;
    @NotNull
    private final RiftLibImpactShape impactShape;

    @NotNull
    private Vec3d start;
    @NotNull
    private Vec3d end;
    @NotNull
    private Vec3d previousStart;
    @NotNull
    private Vec3d previousEnd;
    @NotNull
    private Vec3d direction;
    private double distanceTravelled;

    private boolean impacting;
    @Nullable
    private Vec3d impactCenter;
    @Nullable
    private Vec3d poseUp;
    @Nullable
    private EnumFacing hitFace;
    @Nullable
    private ImpactBasis cachedImpactBasis;
    @Nullable
    private Vec3d impactSurfaceNormal;
    @Nullable
    private Vec3d impactSurfacePoint;
    @NotNull
    private final LinkedHashSet<BlockPos> impactPositions = new LinkedHashSet<>();
    @NotNull
    private final Queue<BlockPos> impactFrontier = new ArrayDeque<>();
    @NotNull
    private final Set<BlockPos> queuedImpactPositions = new HashSet<>();
    @NotNull
    private final Set<BlockPos> evaluatedImpactPositions = new HashSet<>();
    private double previousImpactSize;
    private double currentImpactSize;
    private double maxImpactSize;
    private double decaySize = -1D;
    @Nullable
    private Region<Vector3D> currentImpactRegion;

    private boolean isDead;

    /**
     * Create a segment that starts out in motion.
     */
    public RiftLibRaySegment(
            @NotNull IRayCreator<?> rayCreator,
            @NotNull Vec3d center,
            @NotNull Vec3d direction,
            double length,
            @NotNull RiftLibRayBuilder builder
    ) {
        this.rayCreator = rayCreator;
        this.builder = builder;
        this.impactShape = builder.getImpactShape().get();
        this.direction = direction.normalize();

        double halfLength = Math.max(length, 0.001D) * 0.5D;
        this.start = center.subtract(this.direction.scale(halfLength));
        this.end = center.add(this.direction.scale(halfLength));
        this.previousStart = this.start;
        this.previousEnd = this.end;
    }

    /**
     * Create a segment that starts as an impact.
     */
    public RiftLibRaySegment(
            @NotNull IRayCreator<?> rayCreator,
            @NotNull Vec3d center,
            @NotNull Vec3d incomingDirection,
            @NotNull Vec3d poseUp,
            @NotNull BlockPos hitBlock,
            @Nullable EnumFacing hitFace,
            @NotNull RiftLibRayBuilder builder
    ) {
        this(rayCreator, center, incomingDirection, 0.001D, builder);
        this.beginImpact(center, poseUp, hitBlock, hitFace);
    }

    /**
     * Main update function for segment.
     * */
    public void tick(@NotNull RiftLibRay.RayPose pose, @NotNull Set<BlockPos> hitBlocks, @NotNull Set<Entity> hitEntities) {
        //do not update if dead
        if (this.isDead) return;

        if (this.impacting) this.tickImpact(hitBlocks, hitEntities);
        else this.tickMotion(pose, hitBlocks, hitEntities);
    }

    //-----movement phase stuff starts here-----
    /**
     * For ticking when the ray is in movement
     * */
    private void tickMotion(@NotNull RiftLibRay.RayPose pose, @NotNull Set<BlockPos> hitBlocks, @NotNull Set<Entity> hitEntities) {
        this.previousStart = this.start;
        this.previousEnd = this.end;
        Vec3d oldStart = this.start;

        if (this.builder.getSegmentsFollowParentDirection()) {
            Vec3d center = this.start.add(this.end).scale(0.5D);
            double halfLength = this.end.subtract(this.start).length() * 0.5D;
            this.direction = pose.direction();
            this.start = center.subtract(this.direction.scale(halfLength));
            this.end = center.add(this.direction.scale(halfLength));
        }
        Vec3d leadingEdgeBeforeMovement = this.end;

        double remainingDistance = Math.max(this.builder.getMaxMotionDistance() - this.distanceTravelled, 0D);
        double movementDistance = Math.clamp(this.builder.getMotionSpeed(), 0D, remainingDistance);
        Vec3d movement = this.direction.scale(movementDistance);
        this.start = this.start.add(movement);
        this.end = this.end.add(movement);

        TraceLine line = new TraceLine(oldStart, this.end);
        TraceData trace = this.processLine(line, hitBlocks);
        this.collectEntitiesAlongLine(line.start(), trace.reachedEnd(), hitEntities);

        //stuff to do when segment in motion is blocked by unbreakable block
        if (trace.blocked()) {
            double distanceBeforeImpact = trace.reachedEnd()
                    .subtract(leadingEdgeBeforeMovement)
                    .dotProduct(this.direction);
            this.distanceTravelled += Math.clamp(distanceBeforeImpact, 0D, movementDistance);

            if (this.builder.getHasImpact()) {
                this.beginImpact(
                        trace.reachedEnd(),
                        pose.up(),
                        trace.blockingBlock(),
                        trace.blockingFace()
                );
                if (!this.isDead) this.tickImpact(hitBlocks, hitEntities);
            }
            else this.killSegment();
        }
        //for when segment can still move as normal
        else {
            this.distanceTravelled += movementDistance;
            if (this.distanceTravelled >= this.builder.getMaxMotionDistance()) this.killSegment();
        }
    }

    @NotNull
    private TraceData processLine(@NotNull TraceLine line, @NotNull Set<BlockPos> blocks) {
        for (BlockPos pos : this.blocksBetween(line.start(), line.end())) {
            if (!this.world().isBlockLoaded(pos)) continue;
            blocks.add(pos);
            if (this.isPassable(pos)) continue;
            if (this.builder.getBlockBreakCheck().apply(this.rayCreator, pos)) {
                this.tryBreakBlock(pos);
                continue;
            }

            IBlockState state = this.world().getBlockState(pos);
            AxisAlignedBB localCollisionBox = state.getCollisionBoundingBox(this.world(), pos);
            RayTraceResult hit = null;
            if (localCollisionBox != null) {
                AxisAlignedBB worldCollisionBox = new AxisAlignedBB(
                        localCollisionBox.minX + pos.getX(),
                        localCollisionBox.minY + pos.getY(),
                        localCollisionBox.minZ + pos.getZ(),
                        localCollisionBox.maxX + pos.getX(),
                        localCollisionBox.maxY + pos.getY(),
                        localCollisionBox.maxZ + pos.getZ()
                );
                hit = worldCollisionBox.calculateIntercept(line.start(), line.end());
            }

            Vec3d movement = line.end().subtract(line.start());
            EnumFacing blockingFace = hit != null ? hit.sideHit : null;
            if (blockingFace == null) {
                double absX = Math.abs(movement.x);
                double absY = Math.abs(movement.y);
                double absZ = Math.abs(movement.z);
                if (absY >= absX && absY >= absZ) {
                    blockingFace = movement.y >= 0D ? EnumFacing.DOWN : EnumFacing.UP;
                }
                else if (absX >= absZ) {
                    blockingFace = movement.x >= 0D ? EnumFacing.WEST : EnumFacing.EAST;
                }
                else {
                    blockingFace = movement.z >= 0D ? EnumFacing.NORTH : EnumFacing.SOUTH;
                }
            }

            Vec3d hitVec = hit != null && hit.hitVec != null
                    ? hit.hitVec
                    : new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            return new TraceData(true, hitVec, pos, blockingFace);
        }

        return new TraceData(false, line.end(), null, null);
    }

    /**
     * Get entities that intersect with a line that defines ray segment movement
     * */
    private void collectEntitiesAlongLine(@NotNull Vec3d lineStart, @NotNull Vec3d lineEnd, @NotNull Set<Entity> entities) {
        AxisAlignedBB searchBox = RiftLibRayGeometry.createAABB(lineStart, lineEnd, 0.25D);
        List<Entity> candidates = this.world().getEntitiesWithinAABBExcludingEntity(
                this.rayCreator.getRayCreator(),
                searchBox
        );

        for (Entity entity : candidates) {
            AxisAlignedBB box = entity.getEntityBoundingBox().grow(0.25D);
            RayTraceResult hit = box.calculateIntercept(lineStart, lineEnd);
            if (hit != null || box.contains(lineStart) || box.contains(lineEnd)) entities.add(entity);
        }
    }
    //-----movement phase stuff ends here-----

    //-----impact phase stuff starts here-----
    /**
     * For ticking when the ray is impacting
     * */
    private void tickImpact(@NotNull Set<BlockPos> hitBlocks, @NotNull Set<Entity> hitEntities) {
        if (this.impactCenter == null || this.currentImpactRegion == null) {
            this.killSegment();
            return;
        }

        double impactSpeed = Math.max(this.builder.getMotionSpeed(), 0D);
        this.previousImpactSize = this.currentImpactSize;
        this.currentImpactSize = Math.clamp(
                this.currentImpactSize + impactSpeed,
                this.currentImpactSize,
                this.maxImpactSize
        );
        this.currentImpactRegion = this.impactShape.createRegion(this.currentImpactSize);
        this.spreadImpact(hitBlocks);
        this.collectEntitiesInImpact(hitEntities);
        hitBlocks.addAll(this.impactPositions);

        this.decaySize += impactSpeed;
        if (this.decaySize > 0D) {
            Region<Vector3D> decayRegion = this.impactShape.createRegion(this.decaySize);
            Iterator<BlockPos> positionIterator = this.impactPositions.iterator();
            while (positionIterator.hasNext()) {
                BlockPos pos = positionIterator.next();
                if (!this.impactShape.contains(decayRegion, this.worldBlockCenterToImpactLocal(pos), this.decaySize)) continue;
                positionIterator.remove();
            }
        }

        if (this.currentImpactSize >= this.maxImpactSize && this.impactPositions.isEmpty()) {
            this.killSegment();
        }
    }

    /**
     * Start impact phase
     * */
    private void beginImpact(
            @NotNull Vec3d center, @NotNull Vec3d impactPoseUp,
            @NotNull BlockPos impactBlock, @Nullable EnumFacing impactFace
    ) {
        double maxDistance = Math.max(this.builder.getMaxMotionDistance(), 0D);
        this.maxImpactSize = Math.clamp(maxDistance - this.distanceTravelled, 0D, maxDistance);

        //no max impact size means instant death
        if (this.maxImpactSize == 0D) this.killSegment();
        //go on as normal
        else {
            this.currentImpactSize = 0D;
            this.previousImpactSize = this.currentImpactSize;

            BlockPos originBlock = impactFace == null ? new BlockPos(center) : impactBlock.offset(impactFace);
            this.impactCenter = new Vec3d(
                    originBlock.getX() + 0.5D,
                    originBlock.getY() + 0.5D,
                    originBlock.getZ() + 0.5D
            );
            this.poseUp = impactPoseUp.normalize();
            this.hitFace = impactFace;
            this.cachedImpactBasis = this.impactBasis();
            if (impactFace != null) {
                this.impactSurfaceNormal = RiftLibRayGeometry.facingVector(impactFace);
                Vec3d hitBlockCenter = new Vec3d(
                        impactBlock.getX() + 0.5D,
                        impactBlock.getY() + 0.5D,
                        impactBlock.getZ() + 0.5D
                );
                this.impactSurfacePoint = hitBlockCenter.add(this.impactSurfaceNormal.scale(0.5D));
            }
            this.start = this.impactCenter;
            this.end = this.impactCenter;
            this.previousStart = this.impactCenter;
            this.previousEnd = this.impactCenter;
            this.currentImpactRegion = this.impactShape.createRegion(this.currentImpactSize);
            BlockPos impactOrigin = new BlockPos(this.impactCenter);
            this.impactFrontier.add(impactOrigin);
            this.queuedImpactPositions.add(impactOrigin);
            this.impacting = true;
        }
    }

    /**
     * Grow the set of blocks hit by the impact
     * */
    private void spreadImpact(@NotNull Set<BlockPos> hitBlocks) {
        if (this.impactCenter == null || this.currentImpactRegion == null) return;

        BlockPos origin = new BlockPos(this.impactCenter);
        int extent = this.impactShape.blockExtent(this.currentImpactSize) + 2;
        Queue<BlockPos> deferred = new ArrayDeque<>();

        while (!this.impactFrontier.isEmpty()) {
            BlockPos current = this.impactFrontier.remove();
            this.queuedImpactPositions.remove(current);
            if (!this.isInsideImpactSearchBounds(origin, current, extent)) continue;
            if (!this.impactShape.contains(
                    this.currentImpactRegion,
                    this.worldBlockCenterToImpactLocal(current),
                    this.currentImpactSize
            )) {
                if (this.currentImpactSize < this.maxImpactSize) deferred.add(current);
                else this.evaluatedImpactPositions.add(current);
                continue;
            }

            this.evaluatedImpactPositions.add(current);
            if (!this.isImpactPositionVisible(current)) continue;

            hitBlocks.add(current);
            if (!this.impactPositions.contains(current)) {
                if (!this.canImpactContinueThrough(current)) continue;
                this.impactPositions.add(current);
                this.tryBreakBlock(current);
            }

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos next = current.offset(facing);
                if (this.evaluatedImpactPositions.contains(next)) continue;
                if (this.queuedImpactPositions.add(next)) this.impactFrontier.add(next);
            }
        }

        for (BlockPos pos : deferred) {
            if (this.queuedImpactPositions.add(pos)) this.impactFrontier.add(pos);
        }
    }

    private boolean isImpactPositionVisible(@NotNull BlockPos pos) {
        if (this.impactCenter == null) return false;
        Vec3d targetCenter = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        if (this.impactSurfaceNormal != null && this.impactSurfacePoint != null) {
            if (targetCenter.subtract(this.impactSurfacePoint).dotProduct(this.impactSurfaceNormal) < -1.0E-6D) {
                return false;
            }
        }

        for (BlockPos traversedPos : this.blocksBetween(this.impactCenter, targetCenter)) {
            if (traversedPos.equals(pos)) return true;
            if (this.isPassable(traversedPos)) continue;
            if (this.builder.getBlockBreakCheck().apply(this.rayCreator, traversedPos)) continue;
            return false;
        }
        return true;
    }

    @NotNull
    private Vector3D worldBlockCenterToImpactLocal(@NotNull BlockPos pos) {
        Vec3d worldPoint = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        Vec3d delta = worldPoint.subtract(this.impactCenter);
        ImpactBasis basis = this.impactBasis();
        return Vector3D.of(
                delta.dotProduct(basis.right()),
                delta.dotProduct(basis.up()),
                delta.dotProduct(basis.forward())
        );
    }

    @NotNull
    private ImpactBasis impactBasis() {
        if (this.cachedImpactBasis != null) return this.cachedImpactBasis;

        Vec3d up = this.hitFace == null ? this.poseUp.normalize() : RiftLibRayGeometry.facingVector(this.hitFace).normalize();
        Vec3d forwardSeed = this.hitFace == null ? this.direction : (this.hitFace.getAxis() == EnumFacing.Axis.Y
                                                                     ? new Vec3d(0D, 0D, 1D)
                                                                     : new Vec3d(0D, 1D, 0D));
        if (Math.abs(forwardSeed.dotProduct(up)) > 0.999D) {
            forwardSeed = new Vec3d(1D, 0D, 0D);
        }

        Vec3d right = forwardSeed.crossProduct(up);
        if (right.length() < 1.0E-6D) right = new Vec3d(1D, 0D, 0D);
        right = right.normalize();
        Vec3d forward = up.crossProduct(right).normalize();
        return new ImpactBasis(right, up, forward);
    }

    private void collectEntitiesInImpact(@NotNull Set<Entity> entities) {
        if (this.impactCenter == null || this.impactPositions.isEmpty()) return;

        int extent = this.impactShape.blockExtent(this.currentImpactSize) + 1;
        AxisAlignedBB searchBox = new AxisAlignedBB(
                this.impactCenter.x - extent,
                this.impactCenter.y - extent,
                this.impactCenter.z - extent,
                this.impactCenter.x + extent,
                this.impactCenter.y + extent,
                this.impactCenter.z + extent
        );
        List<Entity> candidates = this.world().getEntitiesWithinAABBExcludingEntity(
                this.rayCreator.getRayCreator(),
                searchBox
        );

        for (Entity entity : candidates) {
            AxisAlignedBB entityBounds = entity.getEntityBoundingBox();
            int minX = (int) Math.floor(entityBounds.minX);
            int minY = (int) Math.floor(entityBounds.minY);
            int minZ = (int) Math.floor(entityBounds.minZ);
            int maxX = (int) Math.ceil(entityBounds.maxX);
            int maxY = (int) Math.ceil(entityBounds.maxY);
            int maxZ = (int) Math.ceil(entityBounds.maxZ);

            boolean hit = false;
            for (int x = minX; x <= maxX && !hit; x++) {
                for (int y = minY; y <= maxY && !hit; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!this.impactPositions.contains(new BlockPos(x, y, z))) continue;
                        hit = true;
                        break;
                    }
                }
            }
            if (hit) entities.add(entity);
        }
    }

    private boolean isInsideImpactSearchBounds(@NotNull BlockPos origin, @NotNull BlockPos pos, int extent) {
        return Math.abs(pos.getX() - origin.getX()) <= extent
                && Math.abs(pos.getY() - origin.getY()) <= extent
                && Math.abs(pos.getZ() - origin.getZ()) <= extent;
    }

    private boolean canImpactContinueThrough(@NotNull BlockPos pos) {
        return this.isPassable(pos) || this.builder.getBlockBreakCheck().apply(this.rayCreator, pos);
    }
    //-----impact phase stuff ends here-----

    private void tryBreakBlock(@NotNull BlockPos pos) {
        World world = this.world();
        IBlockState state = world.getBlockState(pos);
        if (world.isRemote || state.getMaterial() == Material.AIR || state.getMaterial() == Material.FIRE) return;
        if (!this.builder.getBlockBreakCheck().apply(this.rayCreator, pos)) return;
        world.destroyBlock(pos, true);
    }

    @NotNull
    private List<BlockPos> blocksBetween(@NotNull Vec3d lineStart, @NotNull Vec3d lineEnd) {
        List<BlockPos> result = new ArrayList<>();
        int x = (int) Math.floor(lineStart.x);
        int y = (int) Math.floor(lineStart.y);
        int z = (int) Math.floor(lineStart.z);
        int endX = (int) Math.floor(lineEnd.x);
        int endY = (int) Math.floor(lineEnd.y);
        int endZ = (int) Math.floor(lineEnd.z);
        result.add(new BlockPos(x, y, z));

        double dx = lineEnd.x - lineStart.x;
        double dy = lineEnd.y - lineStart.y;
        double dz = lineEnd.z - lineStart.z;
        int stepX = Integer.compare(endX, x);
        int stepY = Integer.compare(endY, y);
        int stepZ = Integer.compare(endZ, z);
        double tMaxX = this.intBound(lineStart.x, dx);
        double tMaxY = this.intBound(lineStart.y, dy);
        double tMaxZ = this.intBound(lineStart.z, dz);
        double tDeltaX = dx == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dx);
        double tDeltaY = dy == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dy);
        double tDeltaZ = dz == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dz);

        while (x != endX || y != endY || z != endZ) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                }
                else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }
            else if (tMaxY < tMaxZ) {
                y += stepY;
                tMaxY += tDeltaY;
            }
            else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
            result.add(new BlockPos(x, y, z));
        }

        return result;
    }

    private double intBound(double value, double delta) {
        if (delta == 0D) return Double.POSITIVE_INFINITY;
        if (delta > 0D) return (Math.floor(value + 1D) - value) / delta;
        return (value - Math.floor(value)) / -delta;
    }

    private boolean isPassable(@NotNull BlockPos pos) {
        IBlockState state = this.world().getBlockState(pos);
        return this.world().isAirBlock(pos)
                || state.getMaterial() == Material.FIRE
                || state.getMaterial().isReplaceable()
                || !state.getMaterial().blocksMovement();
    }

    /**
     * Kill this segment
     * */
    public void killSegment() {
        this.isDead = true;
        this.impactPositions.clear();
        this.impactFrontier.clear();
        this.queuedImpactPositions.clear();
        this.evaluatedImpactPositions.clear();
    }

    public boolean isDead() {
        return this.isDead;
    }

    /**
     * Debug grid lines are the visualization of the ray and are visible when hitbox view is on.
     * They are meant for client use only.
     */
    //-----debug line stuff starts here-----
    public void forEachDebugLine(float partialTicks, @NotNull BiConsumer<Vec3d, Vec3d> lineConsumer) {
        //debug lines for impact phase
        if (this.impacting && this.impactCenter != null) {
            double size = Interpolations.lerp(this.previousImpactSize, this.currentImpactSize, partialTicks);
            Region<Vector3D> region = this.impactShape.createRegion(size);
            int radialSteps = 12;
            int verticalSteps = 6;

            for (int latitude = 0; latitude <= verticalSteps; latitude++) {
                double theta = -Math.PI / 2D + Math.PI * latitude / verticalSteps;
                Vec3d previous = null;

                for (int radial = 0; radial <= radialSteps; radial++) {
                    double angle = Math.TAU * radial / radialSteps;
                    Vector3D boundaryDirection = Vector3D.of(
                            Math.cos(theta) * Math.cos(angle),
                            Math.sin(theta),
                            Math.cos(theta) * Math.sin(angle)
                    );
                    Vec3d current = this.impactBoundaryPoint(region, boundaryDirection, size);
                    this.emitDebugLine(previous, current, lineConsumer);
                    previous = current;
                }
            }

            for (int radial = 0; radial < radialSteps; radial += 2) {
                double angle = Math.TAU * radial / radialSteps;
                Vec3d previous = null;

                for (int latitude = 0; latitude <= verticalSteps; latitude++) {
                    double theta = -Math.PI / 2D + Math.PI * latitude / verticalSteps;
                    Vector3D boundaryDirection = Vector3D.of(
                            Math.cos(theta) * Math.cos(angle),
                            Math.sin(theta),
                            Math.cos(theta) * Math.sin(angle)
                    );
                    Vec3d current = this.impactBoundaryPoint(region, boundaryDirection, size);
                    this.emitDebugLine(previous, current, lineConsumer);
                    previous = current;
                }
            }
        }
        //debug lines for movement phase
        else if (!this.impacting) {
            Vec3d lerpedStartPos = new Vec3d(
                    Interpolations.lerp(this.previousStart.x, this.start.x, partialTicks),
                    Interpolations.lerp(this.previousStart.y, this.start.y, partialTicks),
                    Interpolations.lerp(this.previousStart.z, this.start.z, partialTicks)
            );
            Vec3d lerpedEndPos = new Vec3d(
                    Interpolations.lerp(this.previousEnd.x, this.end.x, partialTicks),
                    Interpolations.lerp(this.previousEnd.y, this.end.y, partialTicks),
                    Interpolations.lerp(this.previousEnd.z, this.end.z, partialTicks)
            );
            lineConsumer.accept(lerpedStartPos, lerpedEndPos);
        }
    }

    private void emitDebugLine(@Nullable Vec3d start, @Nullable Vec3d end, @NotNull BiConsumer<Vec3d, Vec3d> lineConsumer) {
        if (start == null || end == null || start.squareDistanceTo(end) < 1.0E-8D) return;
        lineConsumer.accept(start, end);
    }

    @Nullable
    private Vec3d impactBoundaryPoint(@NotNull Region<Vector3D> region, @NotNull Vector3D boundaryDirection, double size) {
        if (this.impactCenter == null) return null;

        double searchDistance = Math.max(this.impactShape.blockExtent(size) * 2D + 2D, 1D);
        double lastInside = this.impactShape.contains(region, Vector3D.ZERO, size) ? 0D : -1D;
        double firstOutsideAfterInside = searchDistance;
        int searchSteps = 16;

        if (lastInside < 0D) {
            for (int i = 1; i <= searchSteps; i++) {
                double distance = searchDistance * i / searchSteps;
                Vector3D point = boundaryDirection.multiply(distance);
                if (this.impactShape.contains(region, point, size)) {
                    lastInside = distance;
                }
                else if (lastInside >= 0D) {
                    firstOutsideAfterInside = distance;
                    break;
                }
            }
        }

        if (lastInside < 0D) return null;

        double low = lastInside;
        double high = firstOutsideAfterInside;
        for (int i = 0; i < 8; i++) {
            double middle = (low + high) * 0.5D;
            if (this.impactShape.contains(region, boundaryDirection.multiply(middle), size)) low = middle;
            else high = middle;
        }

        Vector3D localPoint = boundaryDirection.multiply(low);
        ImpactBasis basis = this.impactBasis();
        Vec3d boundaryPoint = this.impactCenter
                .add(basis.right().scale(localPoint.getX()))
                .add(basis.up().scale(localPoint.getY()))
                .add(basis.forward().scale(localPoint.getZ()));

        if (this.impactSurfaceNormal != null && this.impactSurfacePoint != null) {
            double centerDistance = this.impactCenter.subtract(this.impactSurfacePoint)
                    .dotProduct(this.impactSurfaceNormal);
            double boundaryDistance = boundaryPoint.subtract(this.impactSurfacePoint)
                    .dotProduct(this.impactSurfaceNormal);
            if (boundaryDistance < 0D) {
                double intersection = Math.clamp(
                        centerDistance / (centerDistance - boundaryDistance),
                        0D,
                        1D
                );
                boundaryPoint = this.impactCenter.add(
                        boundaryPoint.subtract(this.impactCenter).scale(intersection)
                );
            }
        }

        return this.clipDebugBoundaryAtBlocks(boundaryPoint);
    }

    @NotNull
    private Vec3d clipDebugBoundaryAtBlocks(@NotNull Vec3d boundaryPoint) {
        int x = (int) Math.floor(this.impactCenter.x);
        int y = (int) Math.floor(this.impactCenter.y);
        int z = (int) Math.floor(this.impactCenter.z);
        int endX = (int) Math.floor(boundaryPoint.x);
        int endY = (int) Math.floor(boundaryPoint.y);
        int endZ = (int) Math.floor(boundaryPoint.z);
        double dx = boundaryPoint.x - this.impactCenter.x;
        double dy = boundaryPoint.y - this.impactCenter.y;
        double dz = boundaryPoint.z - this.impactCenter.z;
        int stepX = Integer.compare(endX, x);
        int stepY = Integer.compare(endY, y);
        int stepZ = Integer.compare(endZ, z);
        double tMaxX = this.intBound(this.impactCenter.x, dx);
        double tMaxY = this.intBound(this.impactCenter.y, dy);
        double tMaxZ = this.intBound(this.impactCenter.z, dz);
        double tDeltaX = dx == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dx);
        double tDeltaY = dy == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dy);
        double tDeltaZ = dz == 0D ? Double.POSITIVE_INFINITY : Math.abs(1D / dz);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        while (true) {
            pos.setPos(x, y, z);
            if (!this.isPassable(pos) && !this.builder.getBlockBreakCheck().apply(this.rayCreator, pos)) {
                IBlockState state = this.world().getBlockState(pos);
                AxisAlignedBB localCollisionBox = state.getCollisionBoundingBox(this.world(), pos);
                AxisAlignedBB worldCollisionBox = localCollisionBox == null
                        ? new AxisAlignedBB(pos)
                        : new AxisAlignedBB(
                        localCollisionBox.minX + x,
                        localCollisionBox.minY + y,
                        localCollisionBox.minZ + z,
                        localCollisionBox.maxX + x,
                        localCollisionBox.maxY + y,
                        localCollisionBox.maxZ + z
                );
                RayTraceResult hit = worldCollisionBox.calculateIntercept(this.impactCenter, boundaryPoint);
                if (hit != null && hit.hitVec != null) return hit.hitVec;
            }

            if (x == endX && y == endY && z == endZ) break;

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                }
                else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }
            else if (tMaxY < tMaxZ) {
                y += stepY;
                tMaxY += tDeltaY;
            }
            else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }

        return boundaryPoint;
    }
    //-----debug line stuff ends here-----

    /**
     * Helper function for world this segment is created in
     * */
    @NotNull
    private World world() {
        return this.rayCreator.getRayCreator().world;
    }

    /**
     * Represents a line that a ray in movement will travel along
     * */
    public record TraceLine(@NotNull Vec3d start, @NotNull Vec3d end) {}

    /**
     * Result of tracing a moving segment through blocks.
     */
    private record TraceData(
            boolean blocked, @NotNull Vec3d reachedEnd,
            @Nullable BlockPos blockingBlock, @Nullable EnumFacing blockingFace
    ) {}

    /**
     * Local coordinate basis for an impact shape.
     */
    private record ImpactBasis(@NotNull Vec3d right, @NotNull Vec3d up, @NotNull Vec3d forward) {}
}
