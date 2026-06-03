package anightdazingzoroark.riftlib.ray.rayShape;

import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

/**
 * Upper-sphere impact shape. It behaves like the full sphere, but only reaches
 * positions at or above the impact origin.
 * */
public class RiftLibRayUpperSphereImpactShape extends RiftLibRayImpactShape {
    @Override
    public boolean isWithinImpactShape(
            int relX, int relY, int relZ,
            double impactMaxRadius, double impactDepth, @Nullable EnumFacing impactFace
    ) {
        return relY >= 0 && this.isWithinSphere(relX, relY, relZ, impactMaxRadius);
    }
}
