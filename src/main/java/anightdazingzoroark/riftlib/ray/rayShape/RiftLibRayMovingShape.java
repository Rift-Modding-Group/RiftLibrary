package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base shape for rays that move first and then spread cylindrically after impact.
 * */
public class RiftLibRayMovingShape extends RiftLibRayShape {
    @Override
    public void onCreateSegment(@NotNull RiftLibRaySegment segment) {
        segment.setSegmentAABB(new AxisAlignedBB(
                segment.initPos.x - segment.rayWidthRange[0] / 2D,
                segment.initPos.y - segment.rayWidthRange[0] / 2D,
                segment.initPos.z - segment.rayWidthRange[0] / 2D,
                segment.initPos.x + segment.rayWidthRange[0] / 2D,
                segment.initPos.y + segment.rayWidthRange[0] / 2D,
                segment.initPos.z + segment.rayWidthRange[0] / 2D
        ));
    }

    @Override
    public void updateImpact(@NotNull RiftLibRaySegment segment) {
        int maxLayer = (int) Math.ceil(segment.getImpactMaxRadius() + segment.getImpactDepth());

        if (segment.getImpactLayer() < maxLayer && segment.hasImpactFrontier()) {
            segment.setImpactCurrentRadius(segment.getImpactCurrentRadius() + Math.max(0D, segment.getRaySpeed()));
            int targetLayer = Math.min(maxLayer, (int) Math.floor(segment.getImpactCurrentRadius()));

            while (segment.getImpactLayer() < targetLayer && segment.hasImpactFrontier()) {
                List<BlockPos> oldFrontier = new ArrayList<>(segment.getImpactFrontier());
                segment.clearImpactFrontier();
                segment.setImpactLayer(segment.getImpactLayer() + 1);

                for (BlockPos frontierPos : oldFrontier) {
                    for (EnumFacing facing : EnumFacing.values()) {
                        BlockPos nextPos = frontierPos.offset(facing);
                        if (!segment.canImpactReachPosition(nextPos)) continue;
                        segment.tryAddImpactPosition(nextPos);
                    }
                }
            }
        }

        segment.decayImpactPositions();

        if ((segment.getImpactLayer() >= maxLayer || !segment.hasImpactFrontier()) && !segment.hasImpactPositions()) {
            segment.killSegment();
        }
    }

    @Override
    public boolean isWithinImpactShape(
            int relX, int relY, int relZ,
            double impactMaxRadius, double impactDepth, @Nullable EnumFacing impactFace
    ) {
        int faceXOffset = impactFace != null ? impactFace.getXOffset() : 0;
        int faceYOffset = impactFace != null ? impactFace.getYOffset() : 0;
        int faceZOffset = impactFace != null ? impactFace.getZOffset() : 0;

        int inwardDot = -(relX * faceXOffset + relY * faceYOffset + relZ * faceZOffset);
        if (inwardDot < 0 || inwardDot > impactDepth) return false;

        int planeX = relX - inwardDot * -faceXOffset;
        int planeY = relY - inwardDot * -faceYOffset;
        int planeZ = relZ - inwardDot * -faceZOffset;

        return planeX * planeX + planeY * planeY + planeZ * planeZ <= impactMaxRadius * impactMaxRadius;
    }

    @Override
    public boolean isWithinImpactDecayFront(int relX, int relY, int relZ, double impactDecayRadius) {
        return Math.abs(relX) + Math.abs(relY) + Math.abs(relZ) <= impactDecayRadius;
    }

    /**
     * Details what happens upon impacting
     * */
    public void startImpact(@NotNull RiftLibRaySegment segment, @NotNull BlockPos hitBlockPos, @NotNull EnumFacing face) {
        segment.setImpacting();
        segment.setImpactFace(face);
        segment.setImpactMaxRadius(Math.max(1D, segment.maxRayLength - segment.getDistanceTravelled()));
        segment.setImpactDepth(Math.max(1D, segment.getCurrentWidth()));
        segment.setImpactCurrentRadius(0D);
        segment.setImpactDecayRadius(0D);
        segment.getSegmentImpactPositions().clear();
        segment.getExpiredImpactPositions().clear();
        segment.getImpactFrontier().clear();

        BlockPos startPos = hitBlockPos.offset(face);
        segment.setImpactOriginPos(startPos);

        segment.tryAddImpactPosition(startPos);

        double startingRadius = segment.getCurrentWidth() / 2D;
        int startingRadiusBlocks = (int) Math.ceil(startingRadius);

        for (int x = -startingRadiusBlocks; x <= startingRadiusBlocks; x++) {
            for (int y = -startingRadiusBlocks; y <= startingRadiusBlocks; y++) {
                for (int z = -startingRadiusBlocks; z <= startingRadiusBlocks; z++) {
                    //block if not on cylinder impact plane for non impact type rays
                    if (x * face.getXOffset() + y * face.getYOffset() + z * face.getZOffset() == 0) continue;

                    //add once passed
                    segment.tryAddImpactPosition(startPos.add(x, y, z));
                }
            }
        }

        //kill if impact frontier somehow is empty
        if (segment.getImpactFrontier().isEmpty()) segment.killSegment();
    }
}
