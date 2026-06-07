package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public interface IRayCreator<T extends EntityLivingBase & IAnimatable<AnimationDataEntity>> {
    default float rayCreatorScale() {
        return 1f;
    }

    T getRayCreator();

    /**
     * Ray builders are to be defined here.
     * */
    Map<String, RiftLibRayBuilder> getRayBuilders();

    /**
     * When a ray is being applied, its information is sent to the server using this method.
     *
     * @param rayName The name of the ray.
     * @param originPos The BlockPos representation of the origin.
     * @param rayHitResult Holds the entities and block positions hit by the ray
     * */
    void applyRaySegments(String rayName, BlockPos originPos, RiftLibRay.RayHitResult rayHitResult);
}