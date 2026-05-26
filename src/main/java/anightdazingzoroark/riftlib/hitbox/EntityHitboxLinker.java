package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.util.ResourceLocation;

//this class is, just like with the model classes for rendering an entity
//is for assigning files to be linked to a creatures hitboxes
public abstract class EntityHitboxLinker<T extends IAnimatable<AnimationDataEntity> & IMultiHitboxUser<?>> {
    //obvious choice
    public abstract ResourceLocation getHitboxFileLocation(T entity);

    public HitboxDefinitionList getClientHitboxDefinitionList(T animatable) {
        return RiftLibCacheClient.getInstance().getHitboxDefinitions().get(this.getHitboxFileLocation(animatable));
    }

    public HitboxDefinitionList getServerHitboxDefinitionList(T animatable) {
        return RiftLibCacheServer.getInstance().getHitboxDefinitions().get(this.getHitboxFileLocation(animatable));
    }
}