package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Map;

public interface IRayCreator<T extends EntityLivingBase & IAnimatable<AnimationDataEntity>> {
    default float rayCreatorScale() {
        return 1f;
    }

    T getRayCreator();

    /**
     * Rays are to be set here.
     * */
    Map<String, RiftLibRay> getRays();

    /**
     * When a ray is being applied, its information is sent to the server using this method.
     *
     * @param rayName The name of the ray.
     * @param beamCollisionBoxes A list of AxisAlignedBB instances that represent which areas did the beam hit.
     * */
    void applyRayVectorResult(String rayName, List<AxisAlignedBB> beamCollisionBoxes);
}
