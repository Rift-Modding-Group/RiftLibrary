package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link GeoEntityLivingRenderer} for living entities.
 */
@Deprecated(since = "2.1.0")
public abstract class GeoEntityRenderer<A extends EntityLivingBase & IAnimatable<AnimationDataEntity>> extends GeoEntityLivingRenderer<A> {
    public GeoEntityRenderer(@NotNull RenderManager renderManager, @NotNull AnimatedGeoModel<A> modelProvider) {
        super(renderManager, modelProvider);
    }
}
