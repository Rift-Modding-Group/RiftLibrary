package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.propertySystem.propertyStorage.AbstractEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class RiftLibUpdateMultiPropertyKey extends RiftLibMessage<RiftLibUpdateMultiPropertyKey> {
    private int entityId;
    private String setKey;
    private List<String> propertyKeys;
    private NBTTagCompound propertyNbt;

    public RiftLibUpdateMultiPropertyKey() {}

    public RiftLibUpdateMultiPropertyKey(int entityId, String setKey, List<String> propertyKeys, NBTTagCompound propertyNbt) {
        this.entityId = entityId;
        this.setKey = setKey;
        this.propertyKeys = propertyKeys;
        this.propertyNbt = propertyNbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.setKey = ByteBufUtils.readUTF8String(buf);

        NBTTagCompound propertyKeysNBT = ByteBufUtils.readTag(buf);
        if (propertyKeysNBT == null) return;
        this.propertyKeys = this.getPropertyKeysFromNBT(propertyKeysNBT);

        this.propertyNbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.setKey);
        ByteBufUtils.writeTag(buf, this.getPropertyKeysAsNBT(this.propertyKeys));
        ByteBufUtils.writeTag(buf, this.propertyNbt);
    }

    private NBTTagCompound getPropertyKeysAsNBT(List<String> propertyKeys) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();
        for (String propertyKey : propertyKeys) {
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setString("PropertyKey", propertyKey);
            nbtTagList.appendTag(toAppend);
        }
        nbtTagCompound.setTag("PropertyKeyList", nbtTagList);
        return nbtTagCompound;
    }

    private List<String> getPropertyKeysFromNBT(NBTTagCompound nbtTagCompound) {
        List<String> toReturn = new ArrayList<>();
        NBTTagList nbtTagList = nbtTagCompound.getTagList("PropertyKeyList", 10);
        for (int index = 0; index < nbtTagList.tagCount(); index++) {
            NBTTagCompound nbtFromList = nbtTagList.getCompoundTagAt(index);
            if (nbtFromList.isEmpty()) continue;

            toReturn.add(nbtFromList.getString("PropertyKey"));
        }
        return toReturn;
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateMultiPropertyKey message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateMultiPropertyKey message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (entity == null) return;

        AbstractEntityProperties<?> properties = RiftLibProperty.getProperty(message.setKey, entity);
        if (properties == null) return;

        properties.readMultipleFromNBT(message.propertyNbt, message.propertyKeys);
    }
}
