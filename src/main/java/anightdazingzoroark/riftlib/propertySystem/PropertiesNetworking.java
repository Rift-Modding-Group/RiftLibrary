package anightdazingzoroark.riftlib.propertySystem;

import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateMultiPropertyKey;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateSinglePropertyKey;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import java.util.List;

/**
 * Util methods for property networking.
 * */
public class PropertiesNetworking {
    //send single key (server to clients tracking entity)
    public static void sendSingle(Entity entity, String setKey, String propKey, NBTTagCompound propNbt) {
        if (!(entity.world instanceof WorldServer)) return;
        RiftLibUpdateSinglePropertyKey packet = new RiftLibUpdateSinglePropertyKey(entity.getEntityId(), setKey, propKey, propNbt);
        sendToTrackers(entity, packet);
    }

    //send multiple keys (server to clients tracking entity)
    public static void sendMultiple(Entity entity, String setKey, List<String> propKey, NBTTagCompound propNBTs) {
        if (!(entity.world instanceof WorldServer)) return;
        RiftLibUpdateMultiPropertyKey packet = new RiftLibUpdateMultiPropertyKey(entity.getEntityId(), setKey, propKey, propNBTs);
        sendToTrackers(entity, packet);
    }

    private static void sendToTrackers(Entity entity, RiftLibMessage<?> packet) {
        ServerProxy.PROPERTIES_WRAPPER.sendToAllTracking(packet, entity);

        //ensure player gets their own updates too
        if (entity instanceof EntityPlayerMP playerMP) ServerProxy.PROPERTIES_WRAPPER.sendTo(packet, playerMP);
    }
}
