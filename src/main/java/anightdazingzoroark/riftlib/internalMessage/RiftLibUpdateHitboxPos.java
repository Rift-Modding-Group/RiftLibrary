package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibUpdateHitboxPos extends RiftLibMessage<RiftLibUpdateHitboxPos> {
    private int entityId;
    private String hitboxName;
    private float x, y, z;

    public RiftLibUpdateHitboxPos() {}

    public RiftLibUpdateHitboxPos(Entity entity, String hitboxName, float x, float y, float z) {
        this.entityId = entity.getEntityId();
        this.hitboxName = hitboxName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();

        int stringLength = buf.readInt();
        byte[] stringBytes = new byte[stringLength];
        buf.readBytes(stringBytes);
        this.hitboxName = new String(stringBytes);

        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);

        byte[] stringBytes = this.hitboxName.getBytes();
        buf.writeInt(stringBytes.length);
        buf.writeBytes(stringBytes);

        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
        buf.writeFloat(this.z);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateHitboxPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IMultiHitboxUser hitboxUser) {
            hitboxUser.displaceHitboxByAnim(message.hitboxName, message.x, message.y, message.z);
        }
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateHitboxPos message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);

        if (entity instanceof IMultiHitboxUser hitboxUser) {
            hitboxUser.displaceHitboxByAnim(message.hitboxName, message.x, message.y, message.z);
        }
    }
}
