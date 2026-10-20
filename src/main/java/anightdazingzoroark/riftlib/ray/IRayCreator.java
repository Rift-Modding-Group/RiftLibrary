package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataAdvancedEntity;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface IRayCreator<T extends Entity & IAnimatable<? extends AbstractAnimationDataAdvancedEntity<?, ?>>> {
    @NotNull
    T getRayCreator();

    /**
     * Ray builders are to be defined here.
     * */
    @NotNull
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