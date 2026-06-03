package anightdazingzoroark.riftlib.ray.rayShape;

import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

/**
 * Full-sphere impact shape. This preserves the original impact ray behavior.
 * */
public class RiftLibRaySphereImpactShape extends RiftLibRayImpactShape {
    @Override
    public boolean isWithinImpactShape(
            int relX, int relY, int relZ,
            double impactMaxRadius, double impactDepth, @Nullable EnumFacing impactFace
    ) {
        return this.isWithinSphere(relX, relY, relZ, impactMaxRadius);
    }
}
