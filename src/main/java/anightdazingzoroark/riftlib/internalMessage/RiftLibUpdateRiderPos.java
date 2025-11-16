package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftLibUpdateRiderPos extends RiftLibMessage<RiftLibUpdateRiderPos> {
    private int entityId;
    private int index;
    private Vec3d pos;

    public RiftLibUpdateRiderPos() {}

    public RiftLibUpdateRiderPos(Entity entity, int index, Vec3d pos) {
        this.entityId = entity.getEntityId();
        this.index = index;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.index = buf.readInt();
        this.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.index);
        buf.writeDouble(this.pos.x);
        buf.writeDouble(this.pos.y);
        buf.writeDouble(this.pos.z);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateRiderPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IDynamicRideUser) {
            IDynamicRideUser dynamicRideUser = (IDynamicRideUser) entity;
            dynamicRideUser.setRidePosition(message.index, message.pos);
        }
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateRiderPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IDynamicRideUser) {
            IDynamicRideUser dynamicRideUser = (IDynamicRideUser) entity;
            dynamicRideUser.setRidePosition(message.index, message.pos);
        }
    }

    @Override
    public RiftLibMessageSide side() {
        return RiftLibMessageSide.BOTH;
    }
}
