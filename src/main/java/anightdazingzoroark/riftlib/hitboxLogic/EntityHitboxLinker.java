package anightdazingzoroark.riftlib.hitboxLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import net.minecraft.util.ResourceLocation;

//this class is, just like with the model classes for rendering an entity
//is for assigning files to be linked to a creatures hitboxes
public abstract class EntityHitboxLinker<T extends IAnimatable & IMultiHitboxUser> {
    //obvious choice
    public abstract ResourceLocation getHitboxFileLocation(T entity);

    public HitboxDefinitionList getHitboxDefinitionList(T animatable) {
        return RiftLibCache.getInstance().getHitboxDefinitions().get(this.getHitboxFileLocation(animatable));
    }
}