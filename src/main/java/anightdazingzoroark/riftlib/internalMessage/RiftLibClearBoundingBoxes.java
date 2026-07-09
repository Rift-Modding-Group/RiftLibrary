package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibClearBoundingBoxes extends RiftLibMessage<RiftLibClearBoundingBoxes> {
    private int entityId;

    public RiftLibClearBoundingBoxes() {}

    public RiftLibClearBoundingBoxes(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibClearBoundingBoxes message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibClearBoundingBoxes message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IAnimatable<?> animatable && animatable.getAnimationData() instanceof AnimationDataEntity animData)) return;
        animData.clearAllWorldSpaceAABBs();
        animData.clearAllDisplayedAABBs();
    }
}
