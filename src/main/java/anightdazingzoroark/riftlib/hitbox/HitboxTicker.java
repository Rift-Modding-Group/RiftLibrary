package anightdazingzoroark.riftlib.hitbox;

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

public class HitboxTicker {
    private static void manageLoadedUsersHitboxes(HashMap<Integer, List<EntityHitbox>> hitboxMap, World world) {
        List<Integer> hitboxUserIDs = new ArrayList<>();
        for (Entity entity : world.getLoadedEntityList()) {
            if (!(entity instanceof IMultiHitboxUser)) continue;

            //get and put loaded entity ids
            hitboxUserIDs.add(entity.getEntityId());
        }

        //add hitboxes next
        //iterate over every id to see if the entity is loaded
        //if it is already in the map, add it
        //otherwise ignore
        for (int hitboxUserID : hitboxUserIDs) {
            if (hitboxMap.containsKey(hitboxUserID)) continue;

            Entity entity = world.getEntityByID(hitboxUserID);
            List<EntityHitbox> hitboxes = new ArrayList<>();
            if (entity == null) continue;
            for (Entity entityPart : entity.getParts()) {
                if (!(entityPart instanceof EntityHitbox entityHitbox)) continue;
                hitboxes.add(entityHitbox);
            }
            hitboxMap.put(hitboxUserID, hitboxes);
        }
    }

    private static void updateLoadedUsersHitboxes(HashMap<Integer, List<EntityHitbox>> hitboxMap) {
        for (Map.Entry<Integer, List<EntityHitbox>> entry : hitboxMap.entrySet()) {
            //update the hitboxes only
            for (EntityHitbox entityHitbox : entry.getValue()) {
                entityHitbox.onUpdate();
            }
        }
    }

    private static void cleanupLoadedUsersHitboxes(HashMap<Integer, List<EntityHitbox>> hitboxMap) {
        List<Integer> userIDsToRemove = new ArrayList<>();
        outer: for (Map.Entry<Integer, List<EntityHitbox>> entry : hitboxMap.entrySet()) {
            for (EntityHitbox entityHitbox : entry.getValue()) {
                //if the part exists, it might be because its parent still does too
                //so skip them
                if (entityHitbox.isAddedToWorld()) continue outer;
                userIDsToRemove.add(entry.getKey());
            }
        }

        for (int hitboxUserID : userIDsToRemove) hitboxMap.remove(hitboxUserID);
    }

    public static class Server {
        private final HashMap<Integer, List<EntityHitbox>> hitboxMap = new HashMap<>();

        @SubscribeEvent
        public void tickHitboxes(TickEvent.WorldTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            manageLoadedUsersHitboxes(hitboxMap, event.world);
            updateLoadedUsersHitboxes(hitboxMap);
            cleanupLoadedUsersHitboxes(hitboxMap);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            hitboxMap.clear();
        }
    }

    public static class Client {
        private final HashMap<Integer, List<EntityHitbox>> hitboxMap = new HashMap<>();

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void tickHitboxes(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            World world = Minecraft.getMinecraft().world;
            if (world == null) return;

            manageLoadedUsersHitboxes(hitboxMap, world);
            updateLoadedUsersHitboxes(hitboxMap);
            cleanupLoadedUsersHitboxes(hitboxMap);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onWorldUnload(WorldEvent.Unload event) {
            hitboxMap.clear();
        }
    }
}
