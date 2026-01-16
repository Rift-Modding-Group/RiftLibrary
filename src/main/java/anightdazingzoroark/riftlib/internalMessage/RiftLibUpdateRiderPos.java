package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosList;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibUpdateRiderPos extends RiftLibMessage<RiftLibUpdateRiderPos> {
    private int entityId;
    private NBTTagCompound dynamicRidePosListNBT;

    public RiftLibUpdateRiderPos() {}

    public RiftLibUpdateRiderPos(Entity entity, DynamicRidePosList dynamicRidePosList) {
        this.entityId = entity.getEntityId();
        this.dynamicRidePosListNBT = dynamicRidePosList.getAsNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.dynamicRidePosListNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeTag(buf, this.dynamicRidePosListNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateRiderPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);
        DynamicRidePosList dynamicRidePosList = new DynamicRidePosList(message.dynamicRidePosListNBT);

        if (entity instanceof IDynamicRideUser) {
            IDynamicRideUser dynamicRideUser = (IDynamicRideUser) entity;
            dynamicRideUser.setRidePosition(dynamicRidePosList);
        }
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateRiderPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);
        DynamicRidePosList dynamicRidePosList = new DynamicRidePosList(message.dynamicRidePosListNBT);

        if (entity instanceof IDynamicRideUser) {
            IDynamicRideUser dynamicRideUser = (IDynamicRideUser) entity;
            dynamicRideUser.setRidePosition(dynamicRidePosList);
        }
    }
}
