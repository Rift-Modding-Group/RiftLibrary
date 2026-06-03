package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Base behavior for impact-only rays. Concrete subclasses define which portion
 * of the impact sphere can be reached.
 * */
public abstract class RiftLibRayImpactShape extends RiftLibRayShape {
    @Override
    public void onCreateSegment(@NotNull RiftLibRaySegment segment) {
        segment.setImpacting();
        segment.setImpactMaxRadius(segment.rayWidthRange[1]);
        segment.setImpactDepth(Math.max(1D, segment.rayWidthRange[0]));
        segment.setImpactCurrentRadius(segment.rayWidthRange[0] / 2D);
        segment.setImpactDecayRadius(-Math.max(1D, Math.max(0D, segment.getRaySpeed())));
        segment.getSegmentImpactPositions().clear();
        segment.getExpiredImpactPositions().clear();
        segment.getImpactFrontier().clear();

        segment.setImpactOriginPos(new BlockPos(segment.initPos));
        segment.tryAddImpactPosition(segment.getImpactOriginPos());

        double startingRadius = segment.rayWidthRange[0] / 2D;
        int startingRadiusBlocks = (int) Math.ceil(startingRadius);

        for (int x = -startingRadiusBlocks; x <= startingRadiusBlocks; x++) {
            for (int y = -startingRadiusBlocks; y <= startingRadiusBlocks; y++) {
                for (int z = -startingRadiusBlocks; z <= startingRadiusBlocks; z++) {
                    //block if out of reach of this impact-only shape
                    if (!this.isWithinImpactShape(x, y, z, startingRadius, segment.getImpactDepth(), null)) continue;

                    //add once passed
                    segment.tryAddImpactPosition(segment.getImpactOriginPos().add(x, y, z));
                }
            }
        }
    }

    @Override
    public boolean startsAsImpact() {
        return true;
    }

    @Override
    public boolean addsImpactPositionsToFrontier() {
        return false;
    }

    @Override
    public void updateImpact(@NotNull RiftLibRaySegment segment) {
        if (segment.getImpactCurrentRadius() < segment.getImpactMaxRadius()) {
            double previousRadius = segment.getImpactCurrentRadius();
            double targetRadius = Math.min(segment.getImpactMaxRadius(), previousRadius + Math.max(0D, segment.getRaySpeed()));

            int radiusBlocks = (int) Math.ceil(targetRadius);
            double previousRadiusSq = previousRadius * previousRadius;
            double targetRadiusSq = targetRadius * targetRadius;
            BlockPos impactOriginPos = segment.getImpactOriginPos();

            if (impactOriginPos != null) {
                for (int x = -radiusBlocks; x <= radiusBlocks; x++) {
                    for (int y = -radiusBlocks; y <= radiusBlocks; y++) {
                        for (int z = -radiusBlocks; z <= radiusBlocks; z++) {
                            int distanceSq = x * x + y * y + z * z;
                            if (distanceSq <= previousRadiusSq || distanceSq > targetRadiusSq) continue;

                            BlockPos impactPos = impactOriginPos.add(x, y, z);
                            if (!segment.canImpactReachPosition(impactPos)) continue;
                            segment.tryAddImpactPosition(impactPos);
                        }
                    }
                }
            }

            segment.setImpactCurrentRadius(targetRadius);
        }

        segment.decayImpactPositions();

        if (segment.getImpactCurrentRadius() >= segment.getImpactMaxRadius() && !segment.hasImpactPositions()) {
            segment.killSegment();
        }
    }

    @Override
    public boolean isWithinImpactDecayFront(int relX, int relY, int relZ, double impactDecayRadius) {
        return relX * relX + relY * relY + relZ * relZ <= impactDecayRadius * impactDecayRadius;
    }

    protected boolean isWithinSphere(int relX, int relY, int relZ, double radius) {
        return relX * relX + relY * relY + relZ * relZ <= radius * radius;
    }
}
