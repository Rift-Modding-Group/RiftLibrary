package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link GeoEntityLivingLayerRenderer} for living entities.
 */
@Deprecated(since = "2.1.0")
public abstract class GeoLayerRenderer<A extends EntityLivingBase & IAnimatable<AnimationDataEntity>> extends GeoEntityLivingLayerRenderer<A> {
    public GeoLayerRenderer(@NotNull IGeoRenderer<A> entityRenderer) {
        super(entityRenderer);
    }
}
