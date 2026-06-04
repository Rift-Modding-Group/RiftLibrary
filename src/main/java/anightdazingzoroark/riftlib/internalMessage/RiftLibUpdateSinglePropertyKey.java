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

public class RiftLibUpdateSinglePropertyKey extends RiftLibMessage<RiftLibUpdateSinglePropertyKey> {
    private int entityId;
    private String setKey;
    private String propertyKey;
    private NBTTagCompound propertyNbt;

    public RiftLibUpdateSinglePropertyKey() {}

    public RiftLibUpdateSinglePropertyKey(int entityId, String setKey, String propertyKey, NBTTagCompound propertyNbt) {
        this.entityId = entityId;
        this.setKey = setKey;
        this.propertyKey = propertyKey;
        this.propertyNbt = propertyNbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.setKey = ByteBufUtils.readUTF8String(buf);
        this.propertyKey = ByteBufUtils.readUTF8String(buf);
        this.propertyNbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.setKey);
        ByteBufUtils.writeUTF8String(buf, this.propertyKey);
        ByteBufUtils.writeTag(buf, this.propertyNbt);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateSinglePropertyKey message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateSinglePropertyKey message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (entity == null) return;

        AbstractEntityProperties<?> properties = RiftLibProperty.getProperty(message.setKey, entity);
        if (properties == null) return;

        properties.readOneFromNBT(message.propertyNbt, message.propertyKey);
    }
}
