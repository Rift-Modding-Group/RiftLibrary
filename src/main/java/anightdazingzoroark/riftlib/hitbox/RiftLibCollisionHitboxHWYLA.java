package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.RiftLib;
import mcp.mobius.waila.addons.core.HUDHandlerEntities;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * The default HWYLA factory for RiftLibCollisionHitbox instances
 * */
@WailaPlugin(RiftLib.ModID)
public class RiftLibCollisionHitboxHWYLA implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        EntityHitboxProvider hitboxProvider = new EntityHitboxProvider();
        registrar.registerHeadProvider(hitboxProvider, RiftLibCollisionHitbox.class);
        registrar.registerBodyProvider(hitboxProvider, RiftLibCollisionHitbox.class);
        registrar.registerTailProvider(hitboxProvider, RiftLibCollisionHitbox.class);
    }

    private static class EntityHitboxProvider implements IWailaEntityProvider {
        @NotNull
        private final HUDHandlerEntities defaultHandler = new HUDHandlerEntities();

        @Nonnull
        @Override
        public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) entity;
            if (!hitbox.getParent().hitboxUseHWYLA()) return currenttip;

            //get parents provider list
            EntityLivingBase parent = hitbox.getParent().getMultiHitboxUser();
            List<IWailaEntityProvider> parentEntityProviderList = ModuleRegistrar.instance().headEntityProviders.get(parent.getClass());

            //no provider list, use default handler and parent
            if (parentEntityProviderList == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaHead(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> toReturn = new ArrayList<>();
            for (IWailaEntityProvider entityProvider : parentEntityProviderList) {
                toReturn = entityProvider.getWailaHead(entity, toReturn, accessor, config);
            }

            //if toReturn is still empty, default factory
            if (toReturn.isEmpty()) return this.defaultHandler.getWailaHead(parent, toReturn, accessor, config);
            return toReturn;
        }

        @Nonnull
        @Override
        public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) entity;
            if (!hitbox.getParent().hitboxUseHWYLA()) return currenttip;

            //get parents provider list
            EntityLivingBase parent = hitbox.getParent().getMultiHitboxUser();
            List<IWailaEntityProvider> parentEntityProviderList = ModuleRegistrar.instance().headEntityProviders.get(parent.getClass());

            //no provider list, use default handler and parent
            if (parentEntityProviderList == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaBody(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> toReturn = new ArrayList<>();
            for (IWailaEntityProvider entityProvider : parentEntityProviderList) {
                toReturn = entityProvider.getWailaBody(entity, toReturn, accessor, config);
            }

            //if toReturn is still empty, default factory
            if (toReturn.isEmpty()) return this.defaultHandler.getWailaBody(parent, toReturn, accessor, config);
            return toReturn;
        }

        @Nonnull
        @Override
        public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) entity;
            if (!hitbox.getParent().hitboxUseHWYLA()) return currenttip;

            //get parents provider list
            EntityLivingBase parent = hitbox.getParent().getMultiHitboxUser();
            List<IWailaEntityProvider> parentEntityProviderList = ModuleRegistrar.instance().headEntityProviders.get(parent.getClass());

            //no provider list, use default handler and parent
            if (parentEntityProviderList == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaTail(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> toReturn = new ArrayList<>();
            for (IWailaEntityProvider entityProvider : parentEntityProviderList) {
                toReturn = entityProvider.getWailaTail(entity, toReturn, accessor, config);
            }

            //if toReturn is still empty, default factory
            if (toReturn.isEmpty()) return this.defaultHandler.getWailaTail(parent, toReturn, accessor, config);
            return toReturn;
        }
    }
}
