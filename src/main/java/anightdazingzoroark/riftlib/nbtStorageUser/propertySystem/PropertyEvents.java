package anightdazingzoroark.riftlib.nbtStorageUser.propertySystem;

import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateAllPropertyKeys;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.registry.PropertiesBootstrap;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.registry.PropertiesRoot;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.registry.PropertyRegistry;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class PropertyEvents {
    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;
        this.tickProperties(event.world);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.side.isServer() || event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.isGamePaused() || minecraft.world == null) return;
        this.tickProperties(minecraft.world);
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        EntityPlayerMP watcher = (EntityPlayerMP) event.getEntityPlayer();

        for (String name : PropertyRegistry.getAllPropertyNames()) {
            this.syncSetTo(target, watcher, name);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP player)) return;

        //ensure player receives their own full state
        for (String name : PropertyRegistry.getAllPropertyNames()) {
            if (!PropertyRegistry.entityCanHaveProperty(name, player)) continue;
            this.syncSetTo(player, player, name);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        //define old and current properties for player
        PropertiesRoot originalProperties = event.getOriginal().getCapability(PropertiesBootstrap.CAP, null);
        PropertiesRoot currentProperties = event.getEntityPlayer().getCapability(PropertiesBootstrap.CAP, null);

        if (originalProperties == null || currentProperties == null) return;

        //clone original properties into current
        NBTTagCompound nbtToSend = originalProperties.writeToNBT();
        currentProperties.readFromNBT(nbtToSend, event.getEntityPlayer());
    }

    private void syncSetTo(Entity target, EntityPlayerMP watcher, String setKey) {
        AbstractEntityProperties<?> set = RiftLibProperty.getProperty(setKey, target);
        if (set == null) return;

        ServerProxy.PROPERTIES_WRAPPER.sendTo(
                new RiftLibUpdateAllPropertyKeys(target.getEntityId(), setKey, set.writeAllToNBT()),
                watcher
        );
    }

    private void tickProperties(World world) {
        for (Entity entity : new ArrayList<>(world.getLoadedEntityList())) {
            if (!entity.isEntityAlive()) continue;
            for (String name : PropertyRegistry.getAllPropertyNames()) {
                if (!PropertyRegistry.entityCanHaveProperty(name, entity)) continue;
                AbstractEntityProperties<?> properties = RiftLibProperty.getProperty(name, entity);
                if (properties != null) properties.onTickProperty();
            }
        }
    }
}
