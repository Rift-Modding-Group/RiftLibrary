package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibUpdateAllPropertyKeys extends RiftLibMessage<RiftLibUpdateAllPropertyKeys> {
    private int entityId;
    private String key;
    private NBTTagCompound nbtTagCompound;

    public RiftLibUpdateAllPropertyKeys() {}

    public RiftLibUpdateAllPropertyKeys(int entityId, String key, NBTTagCompound nbtTagCompound) {
        this.entityId = entityId;
        this.key = key;
        this.nbtTagCompound = nbtTagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.key = ByteBufUtils.readUTF8String(buf);
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.key);
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateAllPropertyKeys message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateAllPropertyKeys message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (entity == null) return;

        AbstractEntityProperties<?> properties = RiftLibProperty.getProperty(message.key, entity);
        if (properties == null) return;

        properties.readAllFromNBT(message.nbtTagCompound);
    }
}
