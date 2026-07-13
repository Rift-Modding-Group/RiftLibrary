package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibDebugBoundingBox extends RiftLibMessage<RiftLibDebugBoundingBox> {
    private int entityId;
    private String aabbName;
    private boolean add;

    public RiftLibDebugBoundingBox() {}

    public RiftLibDebugBoundingBox(Entity entity, String aabbName, boolean add) {
        this.entityId = entity.getEntityId();
        this.aabbName = aabbName;
        this.add = add;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.aabbName = ByteBufUtils.readUTF8String(buf);
        this.add = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.aabbName);
        buf.writeBoolean(this.add);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibDebugBoundingBox message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibDebugBoundingBox message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IAnimatable<?> animatable && animatable.getAnimationData() instanceof AnimationDataEntity animData)) return;

        if (message.add) animData.createDebugAABB(message.aabbName);
        else animData.removeDebugAABB(message.aabbName);
    }
}
