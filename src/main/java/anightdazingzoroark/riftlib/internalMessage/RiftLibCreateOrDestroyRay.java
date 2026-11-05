package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RiftLibCreateOrDestroyRay extends RiftLibMessage<RiftLibCreateOrDestroyRay> {
    private boolean create;
    private int entityId;
    private String rayName;
    private String locatorName;

    public RiftLibCreateOrDestroyRay() {}

    public RiftLibCreateOrDestroyRay(boolean create, @NotNull Entity rayCreator, @NotNull String rayName, @NotNull String locatorName) {
        this.create = create;
        this.entityId = rayCreator.getEntityId();
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
    public void executeOnServer(MinecraftServer server, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {
        if (client.world == null) return;
        Entity entity = client.world.getEntityByID(message.entityId);
        if (entity == null) return;

        if (message.create) {
            RiftLibRayBuilder rayBuilder = RiftLibRayBuilder.getByName(message.rayName);
            if (rayBuilder == null) {
                RiftLib.LOGGER.warn("Cannot create ray {} because its builder is not defined on the client!", message.rayName);
                return;
            }
            RiftLibRayHelper.createRayOnSide(rayBuilder, message.locatorName, entity);
        }
        else RiftLibRayHelper.killRayOnSide(entity, message.rayName);
    }
}
