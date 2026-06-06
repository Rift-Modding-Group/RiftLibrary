package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RayTicker;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class RiftLibCreateOrDestroyRay extends RiftLibMessage<RiftLibCreateOrDestroyRay> {
    private boolean create;
    private int entityId;
    private String rayName;
    private String locatorName;

    public RiftLibCreateOrDestroyRay() {}

    public RiftLibCreateOrDestroyRay(boolean create, IRayCreator<?> rayCreator, String rayName, String locatorName) {
        this.create = create;
        this.entityId = rayCreator.getRayCreator().getEntityId();
        this.rayName = rayName;
        this.locatorName = locatorName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.create = buf.readBoolean();
        this.entityId = buf.readInt();
        this.rayName = ByteBufUtils.readUTF8String(buf);
        this.locatorName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.create);
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.rayName);
        ByteBufUtils.writeUTF8String(buf, this.locatorName);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = server.getEntityWorld().getEntityByID(message.entityId);
        if (!(entity instanceof IRayCreator<?> rayCreator)) return;

        if (message.create) RiftLibRayHelper.createRayOnSide(rayCreator, message.rayName, message.locatorName);
        else RiftLibRayHelper.killRayOnSide(rayCreator, message.rayName);
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IRayCreator<?> rayCreator)) return;

        if (message.create) RiftLibRayHelper.createRayOnSide(rayCreator, message.rayName, message.locatorName);
        else RiftLibRayHelper.killRayOnSide(rayCreator, message.rayName);
    }
}
