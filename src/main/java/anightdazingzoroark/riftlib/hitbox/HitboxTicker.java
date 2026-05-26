package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.internalMessage.RiftLibSyncHitboxEntityId;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One issue I've had with the hitbox system in previous versions was how there's a good chance that
 * when an entity is removed from the world, so do its hitboxes, mainly from how hitboxes were ticked
 * from the entity. This should fix this by cleaning up orphaned hitboxes that couldn't be removed
 * when the parent was disappeared.
 * */
public class HitboxTicker {
    private final HashMap<Integer, List<EntityHitbox<?>>> hitboxMap = new HashMap<>();

    /**
     * This adds content to the hitbox map.
     * */
    private void manageLoadedUsersHitboxes(World world) {
        List<Integer> hitboxUserIDs = new ArrayList<>();
        //try initialize hitboxes first
        for (Entity entity : world.getLoadedEntityList()) {
            if (!(entity instanceof IMultiHitboxUser<?> multiHitboxUser)) continue;

            multiHitboxUser.setHitboxes();
            if (entity.getParts() == null || entity.getParts().length == 0) continue;

            //get and put loaded entity ids
            hitboxUserIDs.add(entity.getEntityId());
        }

        //add hitboxes next
        //iterate over every id to see if the entity is loaded
        //if it is already in the map, add it
        //otherwise ignore
        for (int hitboxUserID : hitboxUserIDs) {
            if (this.hitboxMap.containsKey(hitboxUserID)) continue;

            Entity entity = world.getEntityByID(hitboxUserID);
            List<EntityHitbox<?>> hitboxes = new ArrayList<>();
            if (entity == null) continue;
            for (Entity entityPart : entity.getParts()) {
                if (!(entityPart instanceof EntityHitbox<?> entityHitbox)) continue;
                hitboxes.add(entityHitbox);
            }
            this.hitboxMap.put(hitboxUserID, hitboxes);
        }
    }

    /**
     * This updates all the hitboxes in the hitbox map.
     * */
    private void updateLoadedUsersHitboxes() {
        for (Map.Entry<Integer, List<EntityHitbox<?>>> entry : this.hitboxMap.entrySet()) {
            //update the hitboxes only
            for (EntityHitbox<?> entityHitbox : entry.getValue()) {
                entityHitbox.onUpdate();
            }
        }
    }

    /**
     * This forcibly syncs from server to all clients the entityIds of the hitboxes.
     * */
    private void updateTrackingClientsWithHitboxEntityIDs(World world) {
        if (world.isRemote || ServerProxy.HITBOX_MESSAGE_WRAPPER == null) return;

        for (Map.Entry<Integer, List<EntityHitbox<?>>> entry : this.hitboxMap.entrySet()) {
            Entity entity = world.getEntityByID(entry.getKey());
            if (entity == null) continue;

            for (EntityHitbox<?> entityHitbox : entry.getValue()) {
                ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToAllTracking(new RiftLibSyncHitboxEntityId(entity, entityHitbox), entity);
            }
        }
    }

    /**
     * Clean up the map to remove dead hitboxes.
     * */
    private void cleanupLoadedUsersHitboxes() {
        List<Integer> userIDsToRemove = new ArrayList<>();
        outer: for (Map.Entry<Integer, List<EntityHitbox<?>>> entry : this.hitboxMap.entrySet()) {
            for (EntityHitbox<?> entityHitbox : entry.getValue()) {
                //if the part exists, it might be because its parent still does too
                //so skip them
                if (entityHitbox.isAddedToWorld()) continue outer;
                userIDsToRemove.add(entry.getKey());
            }
        }

        for (int hitboxUserID : userIDsToRemove) this.hitboxMap.remove(hitboxUserID);
    }

    private void clearMap() {
        this.hitboxMap.clear();
    }

    /**
     * Events for server.
     * */
    public static class Server {
        private final HitboxTicker hitboxTicker;

        public Server() {
            this.hitboxTicker = new HitboxTicker();
        }

        @SubscribeEvent
        public void tickHitboxes(TickEvent.WorldTickEvent event) {
            if (event.side.isClient()) return;
            if (event.phase != TickEvent.Phase.END) return;
            this.hitboxTicker.manageLoadedUsersHitboxes(event.world);
            this.hitboxTicker.updateLoadedUsersHitboxes();
            this.hitboxTicker.updateTrackingClientsWithHitboxEntityIDs(event.world);
            this.hitboxTicker.cleanupLoadedUsersHitboxes();
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            this.hitboxTicker.clearMap();
        }
    }

    /**
     * Events for client.
     * */
    public static class Client {
        private final HitboxTicker hitboxTicker;

        public Client() {
            this.hitboxTicker = new HitboxTicker();
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void tickHitboxes(TickEvent.ClientTickEvent event) {
            if (event.side.isServer()) return;
            if (event.phase != TickEvent.Phase.END) return;

            World world = Minecraft.getMinecraft().world;
            if (world == null) return;

            this.hitboxTicker.manageLoadedUsersHitboxes(world);
            this.hitboxTicker.updateLoadedUsersHitboxes();
            this.hitboxTicker.cleanupLoadedUsersHitboxes();
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onWorldUnload(WorldEvent.Unload event) {
            this.hitboxTicker.clearMap();
        }
    }
}
