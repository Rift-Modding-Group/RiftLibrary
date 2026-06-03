package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This abstract class is the main template for ray shapes. Ray shapes determine
 * ray segment behavior and the shape of the impact.
 * */
public abstract class RiftLibRayShape {
    /**
     * Runs the moment a segment is created.
     * */
    public abstract void onCreateSegment(@NotNull RiftLibRaySegment segment);

    /**
     * Set this to true to make the ray segments always follow the user's rotation.
     * */
    public boolean followUserRotation() {
        return false;
    }

    /**
     * Set this to true to make the ray instantly impact upon creation.
     * */
    public boolean startsAsImpact() {
        return false;
    }

    /**
     * Set this to false when impact positions should not be used as frontier
     * seeds for later spread steps.
     * */
    public boolean addsImpactPositionsToFrontier() {
        return true;
    }

    /**
     * For updating a ray segment when it is impacting.
     * */
    public abstract void updateImpact(@NotNull RiftLibRaySegment segment);

    /**
     * Test if a position is within the impact shape.
     * */
    public abstract boolean isWithinImpactShape(
            int relX, int relY, int relZ,
            double impactMaxRadius, double impactDepth, @Nullable EnumFacing impactFace
    );

    /**
     * Test if a position is within the current decay front and should expire.
     * The relative coordinates are measured from the segment's impact origin.
     * */
    public abstract boolean isWithinImpactDecayFront(int relX, int relY, int relZ, double impactDecayRadius);
}
