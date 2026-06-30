package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

/**
 * One issue I've had with the hitbox system in previous versions was how there's a good chance that
 * when an entity is removed from the world, its hitboxes persist, mainly from how hitboxes were ticked
 * from the entity. This should fix this by cleaning up orphaned hitboxes that couldn't be removed
 * when the parent was disappeared.
 * */
public class HitboxTicker {
    private final List<IMultiHitboxUser<?>> hitboxUserList = new ArrayList<>();

    private void updateTicker(World world) {
        //step 1: add new ones from the world
        world.getEntities(EntityLivingBase.class, entity -> entity instanceof IMultiHitboxUser<?>)
                .forEach(entity -> {
                    if (!this.hitboxUserList.contains(entity)) this.hitboxUserList.add((IMultiHitboxUser<?>) entity);
                });

        //step 2: update all hitboxes and purge hitbox user list of dead entities
        Iterator<IMultiHitboxUser<?>> hitboxUserIterator = this.hitboxUserList.iterator();
        while (hitboxUserIterator.hasNext()) {
            IMultiHitboxUser<?> multiHitboxUser = hitboxUserIterator.next();
            multiHitboxUser.getMultiHitboxList().updateHitboxes();
            if (!multiHitboxUser.getMultiHitboxUser().isEntityAlive()) hitboxUserIterator.remove();
        }
    }

    /**
     * Events for server.
     * */
    public static class Server {
        private final HitboxTicker ticker = new HitboxTicker();

        @SubscribeEvent
        public void tickHitboxes(TickEvent.WorldTickEvent event) {
            if (event.side.isClient()) return;
            if (event.phase != TickEvent.Phase.END) return;

            //iterate over entities to find hitbox users
            this.ticker.updateTicker(event.world);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            this.ticker.hitboxUserList.clear();
        }
    }

    /**
     * Events for client.
     * */
    public static class Client {
        private final HitboxTicker ticker = new HitboxTicker();

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void tickHitboxes(TickEvent.ClientTickEvent event) {
            if (event.side.isServer()) return;
            if (event.phase != TickEvent.Phase.END) return;

            World world = Minecraft.getMinecraft().world;
            if (world == null) return;

            //iterate over entities to find hitbox users
            this.ticker.updateTicker(world);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onWorldUnload(WorldEvent.Unload event) {
            this.ticker.hitboxUserList.clear();
        }
    }
}
