package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.hitboxLogic.IMultiHitboxUser;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftLibUpdateHitboxSize extends RiftLibMessage<RiftLibUpdateHitboxSize> {
    private int entityId;
    private String hitboxName;
    private float width, height;

    public RiftLibUpdateHitboxSize() {}

    public RiftLibUpdateHitboxSize(Entity entity, String hitboxName, float width, float height) {
        this.entityId = entity.getEntityId();
        this.hitboxName = hitboxName;
        this.width = width;
        this.height = height;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();

        int stringLength = buf.readInt();
        byte[] stringBytes = new byte[stringLength];
        buf.readBytes(stringBytes);
        this.hitboxName = new String(stringBytes);

        this.width = buf.readFloat();
        this.height = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);

        byte[] stringBytes = this.hitboxName.getBytes();
        buf.writeInt(stringBytes.length);
        buf.writeBytes(stringBytes);

        buf.writeFloat(this.width);
        buf.writeFloat(this.height);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateHitboxSize message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IMultiHitboxUser) {
            IMultiHitboxUser hitboxUser = (IMultiHitboxUser) entity;
            hitboxUser.updateHitboxScaleFromAnim(message.hitboxName, message.width, message.height);
        }
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateHitboxSize message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IMultiHitboxUser) {
            IMultiHitboxUser hitboxUser = (IMultiHitboxUser) entity;
            hitboxUser.updateHitboxScaleFromAnim(message.hitboxName, message.width, message.height);
        }
    }
}
