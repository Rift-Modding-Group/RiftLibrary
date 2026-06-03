package anightdazingzoroark.riftlib.ray.rayShape;

/**
 * A beam follows the user's rotation while preserving moving-ray impact behavior.
 * */
public class RiftLibRayBeamShape extends RiftLibRayMovingShape {
    @Override
    public boolean followUserRotation() {
        return true;
    }
}
