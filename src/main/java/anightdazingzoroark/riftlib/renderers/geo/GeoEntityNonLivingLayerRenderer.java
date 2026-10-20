package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** A render layer for a non-living animated entity. */
public abstract class GeoEntityNonLivingLayerRenderer<A extends Entity & IAnimatable<AnimationDataEntityNonLiving>> {
    protected IGeoRenderer<A> entityRenderer;

    public GeoEntityNonLivingLayerRenderer(@NotNull IGeoRenderer<A> entityRenderer) {
        this.entityRenderer = entityRenderer;
    }

    @NotNull
    public IGeoRenderer<A> getRenderer() {
        return this.entityRenderer;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public GeoModelProvider<A> getEntityModel() {
        return this.entityRenderer.getGeoModelProvider();
    }

    @NotNull
    protected ResourceLocation getEntityTexture(@NotNull A entity) {
        return this.entityRenderer.getTextureLocation(entity);
    }

    public abstract void render(@NotNull A entity, float partialTicks, float ageInTicks, @NotNull Color renderColor);
}
