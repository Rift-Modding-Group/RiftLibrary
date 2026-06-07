package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibBoxShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibCylinderShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibThreeDimShape;
import net.minecraft.block.state.IBlockState;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base shape for rays that move first and then spread cylindrically after impact.
 * */
public class RiftLibRayMovingShape extends RiftLibRayShape {
    //shape details when the ray is moving
    @Nullable
    private RiftLibBoxShape.Mutable movingShape;
    //shape details when the ray is impacting
    @Nullable
    private RiftLibCylinderShape.Mutable movingImpactShape;

    @Override
    public void onCreateSegment(@NotNull RiftLibRaySegment segment) {
        double xWidth = segment.currentWidth.isThreeDim() ? segment.currentWidth.getWidth(Axis.X) : segment.currentWidth.getWidth();
        double zWidth = segment.currentWidth.isThreeDim() ? segment.currentWidth.getWidth(Axis.Z) : segment.currentWidth.getWidth();
        this.movingShape = new RiftLibBoxShape.Mutable(
                segment.initPos,
                false,
                xWidth,
                Math.max(0.001D, segment.getDistanceTravelled()),
                zWidth
        );
        this.updateMovingShape(segment);
    }

    @Override
    public void updateMovingShape(@NotNull RiftLibRaySegment segment) {
        if (this.movingShape == null) return;

        double length = Math.max(0.001D, segment.getDistanceTravelled());
        double xWidth = segment.currentWidth.isThreeDim() ? segment.currentWidth.getWidth(Axis.X) : segment.currentWidth.getWidth();
        double zWidth = segment.currentWidth.isThreeDim() ? segment.currentWidth.getWidth(Axis.Z) : segment.currentWidth.getWidth();
        this.movingShape.setLengths(xWidth, length, zWidth);

        Quaternion movingShapeRotation = createRotationFromLocalNegativeY(segment.getDirectionVector());
        this.movingShape.rotateShape(movingShapeRotation);
        segment.setSegmentAABB(this.createOrientedBoxAABB(
                segment.initPos,
                this.movingShape.getXLength(),
                this.movingShape.getYLength(),
                this.movingShape.getZLength(),
                movingShapeRotation
            )
        );
    }

    /**
     * Create an impact hit for this moving mesh.
     * */
    @Override
    @Nullable
    public ImmutablePair<BlockPos, EnumFacing> findImpactHit(@NotNull RiftLibRaySegment segment) {
        if (this.movingShape == null) return null;

        int minX = MathHelper.floor(segment.getSegmentAABB().minX);
        int minY = MathHelper.floor(segment.getSegmentAABB().minY);
        int minZ = MathHelper.floor(segment.getSegmentAABB().minZ);
        int maxX = MathHelper.floor(segment.getSegmentAABB().maxX);
        int maxY = MathHelper.floor(segment.getSegmentAABB().maxY);
        int maxZ = MathHelper.floor(segment.getSegmentAABB().maxZ);

        World world = segment.rayCreator.getRayCreator().world;
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

                    if (!segment.getSegmentAABB().intersects(
                            blockBox.minX + x, blockBox.minY + y, blockBox.minZ + z,
                            blockBox.maxX + x, blockBox.maxY + y, blockBox.maxZ + z
                    )) {
                        continue;
                    }
                    if (!this.blockIntersectsShape(segment, testPos, this.movingShape)) continue;

                    Vec3d blockCenter = new Vec3d(x + 0.5D, y + 0.5D, z + 0.5D);
                    double distance = blockCenter.subtract(segment.initPos).dotProduct(segment.getDirectionVector());
                    if (distance >= 0D && distance < bestDistanceSq) {
                        bestDistanceSq = distance;
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
                        (float)-segment.getDirectionVector().x,
                        (float)-segment.getDirectionVector().y,
                        (float)-segment.getDirectionVector().z
                )
        );
    }

    @Override
    public void startImpact(@NotNull RiftLibRaySegment segment, @NotNull BlockPos hitBlockPos, @NotNull EnumFacing face) {
        if (!segment.builder.getSpreadOnHitBlock()) {
            segment.killSegment();
            return;
        }

        segment.setImpacting();
        segment.setImpactMaxRadius(Math.max(1D, segment.builder.getRayMaxLength() - segment.getDistanceTravelled()));
        segment.setImpactDepth(Math.max(1D, segment.getCurrentWidth()));
        segment.setImpactCurrentRadius(0D);
        segment.setImpactDecayRadius(0D);
        segment.getSegmentImpactPositions().clear();
        segment.getExpiredImpactPositions().clear();
        segment.getImpactFrontier().clear();

        BlockPos startPos = hitBlockPos.offset(face);
        segment.setImpactOriginPos(startPos);
        this.impactOriginVec = new Vec3d(startPos.getX() + 0.5D, startPos.getY() + 0.5D, startPos.getZ() + 0.5D);
        this.movingImpactShape = new RiftLibCylinderShape.Mutable(
                this.impactOriginVec,
                false,
                Math.max(0.001D, segment.getImpactCurrentRadius()),
                Math.max(0.001D, segment.getImpactDepth())
        );
        this.impactShape = this.movingImpactShape;
        this.impactShapeRotation = createRotationFromLocalNegativeY(new Vec3d(
                -face.getXOffset(),
                -face.getYOffset(),
                -face.getZOffset()
        ));
        this.updateMovingImpactShape(segment);
        this.addImpactPositionsWithinCurrentShape(segment);
    }

    @Override
    public void updateImpact(@NotNull RiftLibRaySegment segment) {
        if (segment.getImpactCurrentRadius() < segment.getImpactMaxRadius()) {
            double targetRadius = Math.min(
                    segment.getImpactMaxRadius(),
                    segment.getImpactCurrentRadius() + Math.max(0D, segment.builder.getRaySpeed())
            );
            segment.setImpactCurrentRadius(targetRadius);
            this.updateMovingImpactShape(segment);

            //add impact positions in current shape
            this.addImpactPositionsWithinCurrentShape(segment);
        }

        segment.decayImpactPositions();

        if (segment.getImpactCurrentRadius() >= segment.getImpactMaxRadius() && !segment.hasImpactPositions()) {
            segment.killSegment();
        }
    }

    @Override
    public boolean isWithinImpactDecayFront(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        if (this.movingImpactShape == null || segment.getImpactDecayRadius() <= 0D) return false;

        double originalRadius = this.movingImpactShape.getRadius();
        this.movingImpactShape.setRadius(Math.max(0.001D, segment.getImpactDecayRadius()));
        boolean withinDecayFront = this.blockIntersectsShape(segment, pos, this.movingImpactShape);
        this.movingImpactShape.setRadius(originalRadius);
        return withinDecayFront;
    }

    @Override
    @Nullable
    protected RiftLibThreeDimShape getCurrentShape(@NotNull RiftLibRaySegment segment) {
        return segment.isImpacting() ? this.impactShape : this.movingShape;
    }

    //-----debug grid line stuff-----
    @Override
    @NotNull
    protected List<RiftLibRaySegment.DebugLine> createDebugGridLines(@NotNull RiftLibRaySegment segment) {
        if (segment.isImpacting()) return this.createImpactCylinderGridLines();
        return this.createMovingBoxGridLines(segment);
    }

    @NotNull
    private List<RiftLibRaySegment.DebugLine> createMovingBoxGridLines(@NotNull RiftLibRaySegment segment) {
        if (this.movingShape == null) return List.of();
        return this.createTopOriginBoxGridLines(
                segment.initPos,
                this.movingShape.getXLength(),
                this.movingShape.getYLength(),
                this.movingShape.getZLength(),
                this.movingShape.getYXZQuat(),
                segment.getDistanceTravelled()
        );
    }

    @NotNull
    private List<RiftLibRaySegment.DebugLine> createTopOriginBoxGridLines(
            @NotNull Vec3d origin, double xLength, double yLength, double zLength,
            @NotNull Quaternion rotation, double progress
    ) {
        List<RiftLibRaySegment.DebugLine> lines = new ArrayList<>();
        double halfX = xLength / 2D;
        double halfZ = zLength / 2D;
        double gridStep = 0.5D;
        double yOffset = -(progress % gridStep);

        for (double y = yOffset; y >= -yLength; y -= gridStep) {
            Vec3d northWest = new Vec3d(-halfX, y, -halfZ);
            Vec3d northEast = new Vec3d(halfX, y, -halfZ);
            Vec3d southEast = new Vec3d(halfX, y, halfZ);
            Vec3d southWest = new Vec3d(-halfX, y, halfZ);
            this.addTransformedLineLoop(lines, origin, rotation, northWest, northEast, southEast, southWest);
        }

        this.addTransformedLine(lines, origin, rotation, new Vec3d(-halfX, 0D, -halfZ), new Vec3d(-halfX, -yLength, -halfZ));
        this.addTransformedLine(lines, origin, rotation, new Vec3d(halfX, 0D, -halfZ), new Vec3d(halfX, -yLength, -halfZ));
        this.addTransformedLine(lines, origin, rotation, new Vec3d(halfX, 0D, halfZ), new Vec3d(halfX, -yLength, halfZ));
        this.addTransformedLine(lines, origin, rotation, new Vec3d(-halfX, 0D, halfZ), new Vec3d(-halfX, -yLength, halfZ));

        return lines;
    }

    @NotNull
    private List<RiftLibRaySegment.DebugLine> createImpactCylinderGridLines() {
        if (this.impactOriginVec == null || this.movingImpactShape == null) return Collections.emptyList();

        List<RiftLibRaySegment.DebugLine> lines = new ArrayList<>();
        int radialSteps = 24;
        double gridStep = 0.5D;
        double radius = Math.max(0.001D, this.movingImpactShape.getRadius());
        double height = Math.max(0.001D, this.movingImpactShape.getHeight());
        double yOffset = -(radius % gridStep);

        for (double y = yOffset; y >= -height; y -= gridStep) {
            for (int i = 0; i < radialSteps; i++) {
                double angleA = Math.PI * 2D * i / radialSteps;
                double angleB = Math.PI * 2D * (i + 1) / radialSteps;
                lines.add(new RiftLibRaySegment.DebugLine(
                        this.transformShapeOffset(this.impactOriginVec, new Vec3d(Math.cos(angleA) * radius, y, Math.sin(angleA) * radius), this.impactShapeRotation),
                        this.transformShapeOffset(this.impactOriginVec, new Vec3d(Math.cos(angleB) * radius, y, Math.sin(angleB) * radius), this.impactShapeRotation)
                ));
            }
        }

        for (int i = 0; i < radialSteps; i += 2) {
            double angle = Math.PI * 2D * i / radialSteps;
            lines.add(new RiftLibRaySegment.DebugLine(
                    this.transformShapeOffset(this.impactOriginVec, new Vec3d(Math.cos(angle) * radius, 0D, Math.sin(angle) * radius), this.impactShapeRotation),
                    this.transformShapeOffset(this.impactOriginVec, new Vec3d(Math.cos(angle) * radius, -height, Math.sin(angle) * radius), this.impactShapeRotation)
            ));
        }

        return lines;
    }

    //-----helpers beyond this point-----
    private void updateMovingImpactShape(@NotNull RiftLibRaySegment segment) {
        if (this.movingImpactShape == null) return;

        this.movingImpactShape.setRadius(Math.max(0.001D, segment.getImpactCurrentRadius()));
        this.movingImpactShape.setHeight(Math.max(0.001D, segment.getImpactDepth()));
        this.movingImpactShape.rotateShape(this.impactShapeRotation);
        segment.setSegmentAABB(this.createOrientedCylinderAABB(
                this.impactOriginVec != null ? this.impactOriginVec : segment.initPos,
                this.movingImpactShape.getRadius(),
                this.movingImpactShape.getHeight(),
                this.impactShapeRotation
        ));
    }

    @NotNull
    private AxisAlignedBB createOrientedCylinderAABB(
            @NotNull Vec3d origin, double radius, double height, @NotNull Quaternion rotation
    ) {
        List<Vec3d> points = new ArrayList<>();
        int steps = 16;

        for (int i = 0; i < steps; i++) {
            double angle = Math.PI * 2D * i / steps;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            points.add(new Vec3d(x, 0D, z));
            points.add(new Vec3d(x, -height, z));
        }

        return this.createAABBFromOffsets(origin, points, rotation);
    }

    @NotNull
    private AxisAlignedBB createOrientedBoxAABB(
            @NotNull Vec3d origin, double xLength, double yLength, double zLength, @NotNull Quaternion rotation
    ) {
        double halfX = xLength / 2D;
        double halfZ = zLength / 2D;
        List<Vec3d> points = Arrays.asList(
                new Vec3d(-halfX, 0D, -halfZ),
                new Vec3d(-halfX, 0D, halfZ),
                new Vec3d(halfX, 0D, -halfZ),
                new Vec3d(halfX, 0D, halfZ),
                new Vec3d(-halfX, -yLength, -halfZ),
                new Vec3d(-halfX, -yLength, halfZ),
                new Vec3d(halfX, -yLength, -halfZ),
                new Vec3d(halfX, -yLength, halfZ)
        );

        return this.createAABBFromOffsets(origin, points, rotation);
    }

    @NotNull
    private AxisAlignedBB createAABBFromOffsets(@NotNull Vec3d origin, @NotNull List<Vec3d> offsets, @NotNull Quaternion rotation) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        for (Vec3d offset : offsets) {
            Vec3d point = this.transformShapeOffset(origin, offset, rotation);
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            minZ = Math.min(minZ, point.z);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            maxZ = Math.max(maxZ, point.z);
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(0.001D);
    }

    @NotNull
    private Quaternion createRotationFromLocalNegativeY(@NotNull Vec3d targetDirection) {
        Vec3d from = new Vec3d(0D, -1D, 0D);
        Vec3d to = targetDirection.normalize();
        double dot = Math.clamp(from.dotProduct(to), -1D, 1D);

        if (dot > 0.999999D) return new Quaternion();
        if (dot < -0.999999D) return new Quaternion(1F, 0F, 0F, 0F);

        Vec3d axis = from.crossProduct(to);
        double scale = Math.sqrt((1D + dot) * 2D);
        double inverseScale = 1D / scale;
        Quaternion quaternion = new Quaternion(
                (float)(axis.x * inverseScale),
                (float)(axis.y * inverseScale),
                (float)(axis.z * inverseScale),
                (float)(scale * 0.5D)
        );
        Quaternion.normalise(quaternion, quaternion);
        return quaternion;
    }
}
