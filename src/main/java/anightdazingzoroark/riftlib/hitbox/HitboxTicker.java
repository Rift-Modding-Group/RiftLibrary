package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

/** Updates hitboxes for all entity types and cleans up parts of removed parents. */
public class HitboxTicker {
    private Set<IMultiHitboxUser<?>> hitboxUserList = new LinkedHashSet<>();

    private void tickWorld(@NotNull World world) {
        for (Entity entity : new ArrayList<>(world.loadedEntityList)) {
            if (entity.isEntityAlive() && entity instanceof IMultiHitboxUser<?> user) {
                this.hitboxUserList.add(user);
            }
        }

        for (IMultiHitboxUser<?> user : List.copyOf(this.hitboxUserList)) {
            if (user.getWorld() == world) user.getMultiHitboxList().updateHitboxes();
        }
        this.hitboxUserList.removeIf(user -> user.getWorld() == world && !user.getMultiHitboxUser().isEntityAlive());
    }

    private void unloadWorld(@NotNull World world) {
        this.hitboxUserList.removeIf(user -> user.getWorld() == world);
    }

    public static class Server {
        private HitboxTicker ticker = new HitboxTicker();

        @SubscribeEvent
        public void tickHitboxes(TickEvent.WorldTickEvent event) {
            if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;
            this.ticker.tickWorld(event.world);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.getWorld().isRemote) return;
            this.ticker.unloadWorld(event.getWorld());
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Client {
        private HitboxTicker ticker = new HitboxTicker();

        @SubscribeEvent
        public void tickHitboxes(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().isGamePaused()) return;
            World world = Minecraft.getMinecraft().world;
            if (world != null) this.ticker.tickWorld(world);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            this.ticker.unloadWorld(event.getWorld());
        }
    }
}
