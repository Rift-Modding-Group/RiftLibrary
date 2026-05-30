package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.util.ResourceLocation;

//this class is, just like with the model classes for rendering an entity
//is for assigning files to be linked to a creatures hitboxes
public abstract class EntityHitboxLinker<A extends IAnimatable<A, AnimationDataEntity> & IMultiHitboxUser<?>> {
    //obvious choice
    public abstract ResourceLocation getHitboxFileLocation(A entity);

    public HitboxDefinitionList getClientHitboxDefinitionList(A animatable) {
        return RiftLibCacheClient.getInstance().getHitboxDefinitions().get(this.getHitboxFileLocation(animatable));
    }

    public HitboxDefinitionList getServerHitboxDefinitionList(A animatable) {
        return RiftLibCacheServer.getInstance().getHitboxDefinitions().get(this.getHitboxFileLocation(animatable));
    }
}