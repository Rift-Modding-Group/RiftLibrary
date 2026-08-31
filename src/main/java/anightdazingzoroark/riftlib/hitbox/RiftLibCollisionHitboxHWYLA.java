package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.example.entity.DragonEntity;
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
import java.util.Map;

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
            Map<Integer, List<IWailaEntityProvider>> providerGroups = ModuleRegistrar.instance().getHeadEntityProviders(parent);

            //no provider list, use default handler and parent
            if (providerGroups == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaHead(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> result = new ArrayList<>();
            for (List<IWailaEntityProvider> providers : providerGroups.values()) {
                for (IWailaEntityProvider provider : providers) {
                    result = provider.getWailaHead(parent, result, accessor, config);
                }
            }

            currenttip.clear();
            currenttip.addAll(result);
            return currenttip;
        }

        @Nonnull
        @Override
        public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) entity;
            if (!hitbox.getParent().hitboxUseHWYLA()) return currenttip;

            //get parents provider list
            EntityLivingBase parent = hitbox.getParent().getMultiHitboxUser();
            Map<Integer, List<IWailaEntityProvider>> providerGroups = ModuleRegistrar.instance().getBodyEntityProviders(parent);

            //no provider list, use default handler and parent
            if (providerGroups == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaBody(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> result = new ArrayList<>();
            for (List<IWailaEntityProvider> providers : providerGroups.values()) {
                for (IWailaEntityProvider provider : providers) {
                    result = provider.getWailaBody(parent, result, accessor, config);
                }
            }

            currenttip.clear();
            currenttip.addAll(result);
            return currenttip;
        }

        @Nonnull
        @Override
        public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) entity;
            if (!hitbox.getParent().hitboxUseHWYLA()) return currenttip;

            //get parents provider list
            EntityLivingBase parent = hitbox.getParent().getMultiHitboxUser();
            Map<Integer, List<IWailaEntityProvider>> providerGroups = ModuleRegistrar.instance().getTailEntityProviders(parent);

            //no provider list, use default handler and parent
            if (providerGroups == null) {
                currenttip.clear();
                return this.defaultHandler.getWailaTail(parent, currenttip, accessor, config);
            }

            //new string list from parent, assumes parent has waila factory
            List<String> result = new ArrayList<>();
            for (List<IWailaEntityProvider> providers : providerGroups.values()) {
                for (IWailaEntityProvider provider : providers) {
                    result = provider.getWailaTail(parent, result, accessor, config);
                }
            }

            currenttip.clear();
            currenttip.addAll(result);
            return currenttip;
        }
    }
}
