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

public class RiftLibShowBoundingBoxMessage extends RiftLibMessage<RiftLibShowBoundingBoxMessage> {
    private int entityId;
    private String aabbName;
    private boolean add;

    public RiftLibShowBoundingBoxMessage() {}

    public RiftLibShowBoundingBoxMessage(Entity entity, String aabbName, boolean add) {
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
    public void executeOnServer(MinecraftServer server, RiftLibShowBoundingBoxMessage message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibShowBoundingBoxMessage message, EntityPlayer player, MessageContext messageContext) {
        if (client.world == null) return;
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IAnimatable<?> animatable && animatable.getAnimationData() instanceof AnimationDataEntity animData)) return;

        if (message.add) {
            animData.defineWorldSpaceAABB(message.aabbName);
            animData.displayWordSpaceBoundingBox(message.aabbName);
        }
        else {
            animData.removeWorldSpaceAABB(message.aabbName);
            animData.hideWordSpaceBoundingBox(message.aabbName);
        }
    }
}
