package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibSyncHitboxEntityId extends RiftLibMessage<RiftLibSyncHitboxEntityId> {
    private int parentEntityId;
    private String hitboxName;
    private int hitboxEntityId;

    public RiftLibSyncHitboxEntityId() {}

    public RiftLibSyncHitboxEntityId(Entity parent, RiftLibCollisionHitbox<?> hitbox) {
        this.parentEntityId = parent.getEntityId();
        this.hitboxName = hitbox.partName;
        this.hitboxEntityId = hitbox.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.parentEntityId = buf.readInt();

        int stringLength = buf.readInt();
        byte[] stringBytes = new byte[stringLength];
        buf.readBytes(stringBytes);
        this.hitboxName = new String(stringBytes);

        this.hitboxEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.parentEntityId);

        byte[] stringBytes = this.hitboxName.getBytes();
        buf.writeInt(stringBytes.length);
        buf.writeBytes(stringBytes);

        buf.writeInt(this.hitboxEntityId);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibSyncHitboxEntityId message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibSyncHitboxEntityId message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.parentEntityId);
        if (!(entity instanceof IMultiHitboxUser<?> multiHitboxUser)) return;

        multiHitboxUser.setHitboxes();

        RiftLibCollisionHitbox<?> hitbox = multiHitboxUser.getHitboxByName(message.hitboxName);
        if (hitbox == null) return;

        hitbox.syncEntityIdFromServer(message.hitboxEntityId);
    }
}
