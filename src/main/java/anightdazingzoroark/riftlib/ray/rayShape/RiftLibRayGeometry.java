package anightdazingzoroark.riftlib.ray.rayShape;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

public class RiftLibRayGeometry {
    private static final double AABB_PADDING = 0.001D;

    private RiftLibRayGeometry() {}

    @NotNull
    public static Vector3D toVector3D(@NotNull Vec3d vec) {
        return Vector3D.of(vec.x, vec.y, vec.z);
    }

    @NotNull
    public static Vec3d toVec3d(@NotNull Vector3D vec) {
        return new Vec3d(vec.getX(), vec.getY(), vec.getZ());
    }

    @NotNull
    public static QuaternionRotation toQuaternionRotation(@NotNull Quaternion quaternion) {
        return QuaternionRotation.of(quaternion.w, quaternion.x, quaternion.y, quaternion.z);
    }

    @NotNull
    public static AxisAlignedBB toAABB(@NotNull Bounds3D bounds) {
        Vector3D min = bounds.getMin();
        Vector3D max = bounds.getMax();
        return new AxisAlignedBB(
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ()
        ).grow(AABB_PADDING);
    }

    @NotNull
    public static AxisAlignedBB createAABB(@NotNull Vec3d a, @NotNull Vec3d b, double grow) {
        return new AxisAlignedBB(
                Math.min(a.x, b.x),
                Math.min(a.y, b.y),
                Math.min(a.z, b.z),
                Math.max(a.x, b.x),
                Math.max(a.y, b.y),
                Math.max(a.z, b.z)
        ).grow(grow);
    }

    @NotNull
    public static Vec3d facingVector(@NotNull net.minecraft.util.EnumFacing face) {
        return new Vec3d(face.getXOffset(), face.getYOffset(), face.getZOffset());
    }
}
