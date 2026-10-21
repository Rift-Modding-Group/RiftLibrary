package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link GeoEntityLivingModel} for living entities.
 */
@Deprecated(since = "2.1.0")
public class GeoEntityModel<A extends EntityLivingBase & IAnimatable<AnimationDataEntity>> extends GeoEntityLivingModel<A> {
    public GeoEntityModel(@NotNull GeoEntityRenderer<A> renderer) {
        super(renderer);
    }
}
