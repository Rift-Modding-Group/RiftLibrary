package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * One issue I've had with the hitbox system in previous versions was how there's a good chance that
 * when an entity is removed from the world, its hitboxes persist, mainly from how hitboxes were ticked
 * from the entity. This should fix this by cleaning up orphaned hitboxes that couldn't be removed
 * when the parent was disappeared.
 * */
public class HitboxTicker {
    private final List<IMultiHitboxUser<?>> hitboxUserList = new ArrayList<>();

    private void addHitboxUser(@NotNull IMultiHitboxUser<?> iMultiHitboxUser) {
        if (!this.hitboxUserList.contains(iMultiHitboxUser)) this.hitboxUserList.add(iMultiHitboxUser);
    }

    //looks weird, but its to stop this strange cme crash i once got
    private void purgeTicker() {
        //tick everything, including recently-dead parents, usin a snapshot of le hitbox user list
        List<IMultiHitboxUser<?>> snapshot = List.copyOf(this.hitboxUserList);
        for (IMultiHitboxUser<?> user : snapshot) user.getMultiHitboxList().updateHitboxes();

        //clear dead parents
        this.hitboxUserList.removeIf(user -> !user.getMultiHitboxUser().isEntityAlive());
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
            this.ticker.purgeTicker();
        }

        @SubscribeEvent
        public void tickEntities(LivingEvent.LivingUpdateEvent event) {
            EntityLivingBase entityLivingBase = event.getEntityLiving();
            if (entityLivingBase.world.isRemote) return;
            if (entityLivingBase instanceof IMultiHitboxUser<?> iMultiHitboxUser) this.ticker.addHitboxUser(iMultiHitboxUser);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.getWorld().isRemote) return;
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
            this.ticker.purgeTicker();
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void tickEntities(LivingEvent.LivingUpdateEvent event) {
            EntityLivingBase entityLivingBase = event.getEntityLiving();
            if (!entityLivingBase.world.isRemote) return;
            if (entityLivingBase instanceof IMultiHitboxUser<?> iMultiHitboxUser) this.ticker.addHitboxUser(iMultiHitboxUser);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            this.ticker.hitboxUserList.clear();
        }
    }
}
